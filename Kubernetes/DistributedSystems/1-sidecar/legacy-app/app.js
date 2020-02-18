const express = require('express');
const app = express();

const port = process.env.PORT || 8080;

app.get('/', (req, res) => {
	res.send('Hello from legacy app!');
});

app.listen(port, () => {
    console.log(`Application started on port: ${port}`);
});
