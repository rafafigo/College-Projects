namespace ServerGStore {
    public class Obj {
        public bool Lock { get; set; }
        public string Value { get; set; }

        public Obj(string value) {
            Lock = false;
            Value = value;
        }
    }
}
