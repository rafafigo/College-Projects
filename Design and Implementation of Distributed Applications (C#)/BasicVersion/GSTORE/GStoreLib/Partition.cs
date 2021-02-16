using System.Collections.Generic;

namespace GStoreLib {
    public class Partition {
        public string Mid { get; set; }
        public List<string> IdServs { get; set; }

        public Partition(string mid, List<string> idServs) {
            Mid = mid;
            IdServs = idServs;
        }
    }
}
