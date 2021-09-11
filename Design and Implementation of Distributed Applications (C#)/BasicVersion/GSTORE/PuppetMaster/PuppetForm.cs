using System;
using System.Collections.Generic;
using System.IO;
using System.Windows.Forms;

namespace PuppetMaster {
    public partial class PuppetForm : Form {

        private LinkedList<string> commands = new LinkedList<string>();
        private readonly PupExec exec = new PupExec();
        private bool isConfig = false;

        public PuppetForm() {
            InitializeComponent();
        }

        private void BrowseBttn_Click(object sender, EventArgs _) {

            if (isConfig) {
                MessageBox.Show("Already Configured!");
                return;
            }
            if (FileDialog.ShowDialog() != DialogResult.OK) return;
            try {
                commands = new LinkedList<string>(File.ReadAllLines(FileDialog.FileName));
            } catch (IOException e) { MessageBox.Show(e.Message); }
            FilenameTB.Text = FileDialog.FileName;
            isConfig = true;
        }

        private async void ExecuteBttn_Click(object sender, EventArgs _) {

            if (InputTB.Text.Length == 0) {
                MessageBox.Show("No Command Provided!");
                return;
            }
            OutputTB.AppendText(await exec.CommandExec(InputTB.Text));
            InputTB.Clear(); 
        }

        private async void SeqBttn_Click(object sender, EventArgs _) {

            if (FilenameTB.Text.Length == 0) {
                MessageBox.Show("File Not Provided!");
                return;
            }
            if (commands.Count == 0) return;
            foreach (var c in commands) {
                OutputTB.AppendText(await exec.CommandExec(c));
            }
            commands.Clear();
            FilenameTB.Clear();
        }

        private async void StepBttn_Click(object sender, EventArgs _) {

            if (FilenameTB.Text.Length == 0) {
                MessageBox.Show("File Not Provided!");
                return;
            }
            if (commands.Count == 0) return;
            OutputTB.AppendText(await exec.CommandExec(commands.First.Value));
            commands.RemoveFirst();
            if (commands.Count == 0) FilenameTB.Clear();
        }
    }
}
