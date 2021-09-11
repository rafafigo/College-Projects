namespace ServerGStore {
    public class ServerInfo {

        public string Mid { get; }
        public int Tag { get; }
        public int Term { get; }

        public ServerInfo(string mid, int tag, int term) {
            Mid = mid;
            Tag = tag;
            Term = term;
        }
    }
}
