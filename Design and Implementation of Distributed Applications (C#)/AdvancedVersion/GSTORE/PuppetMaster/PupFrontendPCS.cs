using System;
using System.Threading.Tasks;
using System.Windows.Forms;
using Google.Protobuf.Collections;
using Grpc.Core;
using Grpc.Net.Client;

namespace PuppetMaster {
    class PupFrontendPCS {

        private readonly GrpcChannel channel;
        private readonly PCSServices.PCSServicesClient stub;

        public PupFrontendPCS(string URL) {
            channel = GrpcChannel.ForAddress(URL);
            stub = new PCSServices.PCSServicesClient(channel);
        }

        public async Task NewServerAsync(string idServ, string URL, int minDelay, int maxDelay) {
            var req = new NewServerRequest {
                IdServ = idServ,
                URL = URL,
                MinDelay = minDelay,
                MaxDelay = maxDelay
            };
            SetPartitions(req.SParts);
            SetServers(req.Servs);
            try {
                await stub.NewServerAsync(req);
            } catch (RpcException e) {
                PupExec.Servs[idServ].NegAvailSync();
                MessageBox.Show($"NewServerAsync: Not Created!{Environment.NewLine}{e.Message}");
            }
        }

        public async Task NewClientAsync(string uname, string URL, string script) {
            var req = new NewClientRequest {
                Uname = uname,
                URL = URL,
                Script = script
            };
            SetPartitions(req.SParts);
            SetServers(req.Servs);
            try {
                await stub.NewClientAsync(req);
            } catch (RpcException e) {
                PupExec.Clients[uname].NegAvailSync();
                MessageBox.Show($"NewClientAsync: Not Created!{Environment.NewLine}{e.Message}");
            }
        }

        private void SetPartitions(RepeatedField<Partition> res) {
            
            foreach (var idPart in PupExec.SParts.Keys) {
                var p = new Partition {
                    IdPart = idPart
                };
                foreach (var s in PupExec.SParts[idPart].IdServs) p.IdServs.Add(s);
                res.Add(p);
            }
        }

        private void SetServers(MapField<string, string> res) {
            
            foreach (var s in PupExec.Servs) res.Add(s.Key, s.Value.URL);
        }
    }
}
