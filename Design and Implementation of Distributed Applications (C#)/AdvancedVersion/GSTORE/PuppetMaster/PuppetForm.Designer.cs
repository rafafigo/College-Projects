namespace PuppetMaster {
    partial class PuppetForm {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        ///  Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        ///  Required method for Designer support - do not modify
        ///  the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent() {
            this.InputTB = new System.Windows.Forms.TextBox();
            this.OutputTB = new System.Windows.Forms.TextBox();
            this.ExecuteBttn = new System.Windows.Forms.Button();
            this.FileDialog = new System.Windows.Forms.OpenFileDialog();
            this.BrowseBttn = new System.Windows.Forms.Button();
            this.FilenameTB = new System.Windows.Forms.TextBox();
            this.SeqBttn = new System.Windows.Forms.Button();
            this.StepBttn = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // InputTB
            // 
            this.InputTB.BackColor = System.Drawing.Color.Gray;
            this.InputTB.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.InputTB.ForeColor = System.Drawing.SystemColors.Window;
            this.InputTB.Location = new System.Drawing.Point(61, 357);
            this.InputTB.Name = "InputTB";
            this.InputTB.Size = new System.Drawing.Size(503, 29);
            this.InputTB.TabIndex = 0;
            // 
            // OutputTB
            // 
            this.OutputTB.BackColor = System.Drawing.Color.Gray;
            this.OutputTB.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.OutputTB.ForeColor = System.Drawing.Color.White;
            this.OutputTB.Location = new System.Drawing.Point(61, 34);
            this.OutputTB.Multiline = true;
            this.OutputTB.Name = "OutputTB";
            this.OutputTB.ReadOnly = true;
            this.OutputTB.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.OutputTB.Size = new System.Drawing.Size(602, 305);
            this.OutputTB.TabIndex = 7;
            this.OutputTB.TabStop = false;
            // 
            // ExecuteBttn
            // 
            this.ExecuteBttn.BackColor = System.Drawing.SystemColors.Control;
            this.ExecuteBttn.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.ExecuteBttn.Location = new System.Drawing.Point(570, 357);
            this.ExecuteBttn.Name = "ExecuteBttn";
            this.ExecuteBttn.Size = new System.Drawing.Size(93, 29);
            this.ExecuteBttn.TabIndex = 1;
            this.ExecuteBttn.Text = "Execute";
            this.ExecuteBttn.UseVisualStyleBackColor = false;
            this.ExecuteBttn.Click += new System.EventHandler(this.ExecuteBttn_Click);
            // 
            // FileDialog
            // 
            this.FileDialog.FileName = "FileDialog";
            // 
            // BrowseBttn
            // 
            this.BrowseBttn.BackColor = System.Drawing.SystemColors.Control;
            this.BrowseBttn.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.BrowseBttn.Location = new System.Drawing.Point(61, 406);
            this.BrowseBttn.Name = "BrowseBttn";
            this.BrowseBttn.Size = new System.Drawing.Size(94, 29);
            this.BrowseBttn.TabIndex = 3;
            this.BrowseBttn.Text = "Browse";
            this.BrowseBttn.UseVisualStyleBackColor = false;
            this.BrowseBttn.Click += new System.EventHandler(this.BrowseBttn_Click);
            // 
            // FilenameTB
            // 
            this.FilenameTB.BackColor = System.Drawing.Color.Silver;
            this.FilenameTB.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.FilenameTB.Location = new System.Drawing.Point(161, 407);
            this.FilenameTB.Name = "FilenameTB";
            this.FilenameTB.ReadOnly = true;
            this.FilenameTB.Size = new System.Drawing.Size(304, 29);
            this.FilenameTB.TabIndex = 6;
            this.FilenameTB.TabStop = false;
            // 
            // SeqBttn
            // 
            this.SeqBttn.BackColor = System.Drawing.SystemColors.Control;
            this.SeqBttn.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.SeqBttn.Location = new System.Drawing.Point(570, 406);
            this.SeqBttn.Name = "SeqBttn";
            this.SeqBttn.Size = new System.Drawing.Size(93, 30);
            this.SeqBttn.TabIndex = 5;
            this.SeqBttn.Text = "Sequential";
            this.SeqBttn.UseVisualStyleBackColor = false;
            this.SeqBttn.Click += new System.EventHandler(this.SeqBttn_Click);
            // 
            // StepBttn
            // 
            this.StepBttn.BackColor = System.Drawing.SystemColors.Control;
            this.StepBttn.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point);
            this.StepBttn.Location = new System.Drawing.Point(471, 407);
            this.StepBttn.Name = "StepBttn";
            this.StepBttn.Size = new System.Drawing.Size(93, 29);
            this.StepBttn.TabIndex = 4;
            this.StepBttn.Text = "Step";
            this.StepBttn.UseVisualStyleBackColor = false;
            this.StepBttn.Click += new System.EventHandler(this.StepBttn_Click);
            // 
            // PuppetForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.Silver;
            this.ClientSize = new System.Drawing.Size(711, 465);
            this.Controls.Add(this.StepBttn);
            this.Controls.Add(this.SeqBttn);
            this.Controls.Add(this.FilenameTB);
            this.Controls.Add(this.BrowseBttn);
            this.Controls.Add(this.ExecuteBttn);
            this.Controls.Add(this.OutputTB);
            this.Controls.Add(this.InputTB);
            this.Name = "PuppetForm";
            this.Text = "PuppetForm";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox InputTB;
        private System.Windows.Forms.TextBox OutputTB;
        private System.Windows.Forms.Button ExecuteBttn;
        private System.Windows.Forms.OpenFileDialog FileDialog;
        private System.Windows.Forms.Button BrowseBttn;
        private System.Windows.Forms.TextBox FilenameTB;
        private System.Windows.Forms.Button SeqBttn;
        private System.Windows.Forms.Button StepBttn;
    }
}
