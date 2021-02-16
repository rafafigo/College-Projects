using System.Collections.Generic;

namespace GStoreLib {
    public class Partition {
        private string mid;
        public string Mid { get { lock (this) return mid; } set { lock (this) mid = value; } }
        public List<string> IdServs { get; set; }

        public Partition(string mid, List<string> idServs) {
            this.mid = mid;
            IdServs = idServs;
        }
    }
}
