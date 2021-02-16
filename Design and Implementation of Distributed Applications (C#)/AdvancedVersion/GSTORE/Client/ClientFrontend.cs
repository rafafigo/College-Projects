using System;
using GStoreLib;
using Grpc.Core;
using Grpc.Net.Client;
using System.Collections.Generic;

namespace ClientGStore {
    class ClientFrontend {
        public string IdServ { get; set; }
        private readonly GrpcChannel channel;
        private readonly GStoreServices.GStoreServicesClient stub;

        public ClientFrontend(string idServ, string URL) {
            IdServ = idServ;
            channel = GrpcChannel.ForAddress(URL);
            stub = new GStoreServices.GStoreServicesClient(channel);
        }

        public KeyValuePair<int, string> Read(string idPart, string idObj) {
            try {
                var res = stub.Read(new ReadRequest() {
                    IdPart = idPart,
                    IdObj = idObj
                });
                return new KeyValuePair<int, string>(res.Tag, res.Val);
            } catch (RpcException) {
                NegAvail();
                return new KeyValuePair<int, string>(0, null);
            }
        }

        public string Write(string idPart, string idObj, string val) {
            try {
                return stub.Write(new WriteRequest() {
                    IdPart = idPart,
                    IdObj = idObj,
                    Val = val
                }, deadline: DateTime.UtcNow.AddSeconds(5)).Mid;
            } catch (RpcException e) {
                if (e.StatusCode != StatusCode.DeadlineExceeded) NegAvail();
                return null;
            }
        }

        public IDictionary<string, string> ListServer() {

            ListServerReply res;
            try {
                res = stub.ListServer(new ListServerRequest());
            } catch (RpcException) {
                NegAvail();
                return null;
            }

            var objs = new Dictionary<string, string>();
            foreach (var e in res.Objs) objs.Add(e.Key, e.Value);
            return objs;
        }

        private void NegAvail() {
            lock (this) {
                if (ClientManager.Servs[IdServ].GetAvailSync()) {
                    foreach (var p in ClientManager.SParts) {
                        if (p.Value.IdServs.Contains(IdServ)) ClientManager.PInfos[p.Key].Down();
                    }
                    ClientManager.Servs[IdServ].NegAvailSync();
                    Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                }
            }
        }
    }
}
