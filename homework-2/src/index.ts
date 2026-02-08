import app from './app';

const PORT = process.env['PORT'] ?? 3000;

app.listen(PORT, () => {
  console.log(`🚀 Server is running on port ${PORT}`);
  console.log(`📡 Health check: http://localhost:${PORT}/health`);
  console.log(`🎫 Tickets API: http://localhost:${PORT}/tickets`);
});
