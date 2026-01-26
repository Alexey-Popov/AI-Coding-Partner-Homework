#!/bin/bash

# Banking Application Setup Script

set -e

echo "======================================"
echo "Banking Application Setup"
echo "======================================"
echo ""

# Check prerequisites
echo "Checking prerequisites..."

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.11 or higher."
    exit 1
fi
echo "✅ Python found: $(python3 --version)"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi
echo "✅ Node.js found: $(node --version)"

# Check npm
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed."
    exit 1
fi
echo "✅ npm found: $(npm --version)"

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java Runtime Environment."
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

echo ""
echo "======================================"
echo "Downloading H2 Database JAR..."
echo "======================================"

if [ ! -f "h2.jar" ]; then
    echo "Downloading H2 JAR..."
    curl -o h2.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
    echo "✅ H2 JAR downloaded"
else
    echo "✅ H2 JAR already exists"
fi

echo ""
echo "======================================"
echo "Setting up Backend..."
echo "======================================"

cd bank_api

# Create virtual environment
if [ ! -d "venv" ]; then
    echo "Creating Python virtual environment..."
    python3 -m venv venv
    echo "✅ Virtual environment created"
else
    echo "✅ Virtual environment already exists"
fi

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Install Python dependencies
echo "Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt
echo "✅ Python dependencies installed"

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    echo "Creating .env file..."
    cp .env.example .env
    echo "✅ .env file created (please edit with your settings)"
else
    echo "✅ .env file already exists"
fi

cd ..

echo ""
echo "======================================"
echo "Setting up Frontend..."
echo "======================================"

cd ui

# Install npm dependencies
echo "Installing npm dependencies..."
npm install
echo "✅ npm dependencies installed"

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    echo "Creating .env file..."
    cp .env.example .env
    echo "✅ .env file created"
else
    echo "✅ .env file already exists"
fi

cd ..

echo ""
echo "======================================"
echo "Setup Complete! 🎉"
echo "======================================"
echo ""
echo "To start the application:"
echo ""
echo "1. Start the backend (in one terminal):"
echo "   cd bank_api"
echo "   source venv/bin/activate"
echo "   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000"
echo ""
echo "2. Start the frontend (in another terminal):"
echo "   cd ui"
echo "   npm run dev"
echo ""
echo "Then open http://localhost:5173 in your browser"
echo ""
echo "API Documentation: http://localhost:8000/docs"
echo ""
