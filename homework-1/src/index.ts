import app from './app';

const PORT: number = parseInt(process.env.PORT || '3000', 10);

// Start server
app.listen(PORT, () => {
  console.log(`Banking Transactions API running on http://localhost:${PORT}`);
  console.log(`API Documentation available at http://localhost:${PORT}/`);
});
