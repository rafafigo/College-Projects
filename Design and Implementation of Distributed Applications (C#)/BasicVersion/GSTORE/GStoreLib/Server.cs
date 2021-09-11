namespace GStoreLib {
    public class Server {
        public string URL { get; set; }
        public int MinDelay { get; set; }
        public int MaxDelay { get; set; }
        public bool Available { get; set; }

        public Server(string URL) {
            this.URL = URL;
            Available = true;
        }

        public Server(string URL, int minDelay, int maxDelay) : this(URL) {
            MinDelay = minDelay;
            MaxDelay = maxDelay;
        }

        public bool GetAvailSync() {
            lock (this) return Available;
        }
        public void NegAvailSync() {
            lock (this) Available = false;
        }
    }
}
