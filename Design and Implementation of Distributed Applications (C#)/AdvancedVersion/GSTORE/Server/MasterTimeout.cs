using System.Timers;

namespace ServerGStore {
    class MasterTimeout {

        private readonly ServerManager man;
        private readonly string idPart;

        public MasterTimeout(ServerManager man, string idPart) {
            this.man = man;
            this.idPart = idPart;
        }

        public void Timeout(object _, ElapsedEventArgs e) {
            if (man.IsFreeze) return;
            if (ServerManager.RInfos[idPart].IsMaster()) man.SendHeartbeat(idPart);
            else man.BecomeCandidate(idPart);
        }

        public static Timer NewTimer(ServerManager man, string idPart, bool isMaster) {

            var aTimer = new Timer(RaftInfo.TIMEOUT + (isMaster ? 0 : RaftInfo.GetRandomDelay(1)));
            aTimer.Elapsed += new ElapsedEventHandler(new MasterTimeout(man, idPart).Timeout);
            aTimer.AutoReset = true;
            aTimer.Start();
            return aTimer;
        }
    }
}
