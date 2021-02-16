namespace GStoreLib {
    public class Client {
        public string URL { get; set; }
        public bool Available { get; set; }

        public Client(string URL) {
            this.URL = URL;
            Available = true;
        }

        public bool GetAvailSync() {
            lock (this) return Available;
        }
        public void NegAvailSync() {
            lock (this) Available = false;
        }
    }
}
