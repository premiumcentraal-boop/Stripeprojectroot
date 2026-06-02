const fs = require('fs');
fs.rmSync('../android/gradlew', { recursive: true, force: true });
console.log('Deleted gradlew directory');
