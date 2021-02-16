namespace GStoreLib {
    public class PartitionInfo {

        private int avail;
        private int tag;

        public int Avail { get { lock (this) return avail; } set { lock (this) avail = value; } }
        public int Tag { get { lock (this) return tag; } set { lock (this) tag = value; } }

        public PartitionInfo(int avail) {
            this.avail = avail;
            tag = 0;
        }

        public int GetQuorum() {
            return Avail / 2 + 1;
        }

        public void Down() {
            lock (this) avail--;
        }

        public int DecrTag() {
            lock (this) return --tag;
        }

        public int IncrTag() {
            lock (this) return ++tag;
        }
    }
}
