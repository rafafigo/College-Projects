using Grpc.Core;
using Grpc.Net.Client;
using System.Collections.Generic;
using GStoreLib;

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

        public string Read(string idPart, string idObj) {
            try {
                return stub.Read(new ReadRequest() {
                    IdPart = idPart,
                    IdObj = idObj
                }).Val;
            } catch (RpcException) {
                ClientManager.Servs[IdServ].NegAvailSync();
                Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                return null;
            }
        }

        public int Write(string idPart, string idObj, string val) {
            try {
                stub.Write(new WriteRequest() {
                    IdPart = idPart,
                    IdObj = idObj,
                    Val = val
                });
                return 0;
            } catch (RpcException) {
                ClientManager.Servs[IdServ].NegAvailSync();
                Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                return -1;
            }
        }

        public IDictionary<string, string> ListServer() {

            ListServerReply res;
            try {
                res = stub.ListServer(new ListServerRequest());
            } catch (RpcException) {
                ClientManager.Servs[IdServ].NegAvailSync();
                Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                return null;
            }

            var objs = new Dictionary<string, string>();
            foreach (var e in res.Objs) objs.Add(e.Key, e.Value);
            return objs;
        }
    }
}
