using Grpc.Core;
using Grpc.Net.Client;
using GStoreLib;

namespace ServerGStore {
    public class ServerFrontend {
        public string IdServ { get; set; }
        private readonly GrpcChannel channel;
        private readonly GStoreSync.GStoreSyncClient stub;

        public ServerFrontend(string idServ, string URL) {
            IdServ = idServ;
            channel = GrpcChannel.ForAddress(URL);
            stub = new GStoreSync.GStoreSyncClient(channel);
        }

        public int Lock(string idPart, string idObj) {
            try {
                stub.Lock(new LockRequest {
                    IdPart = idPart,
                    IdObj = idObj
                });
                return 0;
            } catch (RpcException) {
                ServerManager.Servs[IdServ].NegAvailSync();
                Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                return -1;
            }
        }

        public int Update(string idPart, string idObj, string val) {
            try {
                stub.Update(new UpdateRequest {
                    IdPart = idPart,
                    IdObj = idObj,
                    Val = val
                });
                return 0;
            } catch (RpcException) {
                ServerManager.Servs[IdServ].NegAvailSync();
                Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                return -1;
            }
        }
    }
}
