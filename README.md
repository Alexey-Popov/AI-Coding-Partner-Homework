# 🤖 AI-Assisted Development Course — Homework Repository

Welcome to the homework repository for the **AI as a Personalized Coding Partner** course! This repository serves as a template for submitting your homework assignments throughout the program.

> 💡 **Pro Tip**: Star this repository to easily find it later!

---

## 🚀 Getting Started

### Step 1: Fork This Repository

1. Click the **Fork** button in the top-right corner of this repository page
2. Select your personal GitHub account as the destination
3. Wait for GitHub to create your personal copy of the repository

### Step 2: Clone Your Fork

```bash
git clone https://github.com/YOUR_USERNAME/ai-assisted-dev-homework.git
```

---

## 📁 Repository Structure

```
ai-assisted-dev-homework/
├── 📄 README.md                    # This file
├── 📂 homework-1/                  # Homework 1: Simple API with AI Assistance
│   ├── 📄 README.md               # Your documentation for HW1
│   ├── 📂 src/                    # Your source code
│   ├── 📂 docs/                   # Additional documentation
│   │   └── 📂 screenshots/        # Screenshots demonstrating AI usage
│   └── 📂 demo/                   # Demo files and run scripts
├── 📂 homework-2/                  # Homework 2: Enhanced App with Tests
├── 📂 homework-3/                  # Homework 3: App from Specification
├── 📂 homework-4/                  # Homework 4: Multi-Agent System
├── 📂 homework-5/                  # Homework 5: MCP Server Configuration
└── 📂 homework-6/                  # Homework 6: Capstone Project
```

---

## 📤 How to Submit Your Homework

### 1️⃣ Create a Branch for Each Assignment

```bash
# For homework 1
git checkout -b homework-1-submission

# Work on your assignment...

git add .
git commit -m "Complete homework 1"
git push origin homework-1-submission
```

### 2️⃣ Create a Pull Request

1. Go to your forked repository on GitHub
2. Click **"Compare & pull request"** or go to **Pull requests** → **New pull request**
3. Set the base repository to the **your personal repository**
4. Set the base branch to `main`
5. Set the compare branch to your `homework-X-submission` branch
6. Fill in the PR template with:
   - ✅ Summary of what you implemented
   - 🛠️ AI tools used
   - ⚠️ Challenges encountered
   - 📸 Screenshots/demos

### 3️⃣ Assign the Instructor for Review

1. In the Pull Request, click **"Reviewers"** on the right sidebar
2. Search for and add the instructor's GitHub username
3. Optionally add labels like `homework-1`, `ready-for-review`

---

## 📋 Submission Requirements

Each homework submission **MUST** include:

### 📝 Required Documentation

| Item | Description |
|------|-------------|
| `README.md` | Clear explanation of your solution, approach, and AI tools used |
| `HOWTORUN.md` | Step-by-step guide to run your application |

### 📸 Screenshots *(Highly Expected)*

Include screenshots demonstrating:
- 🤖 AI tool interactions (prompts and responses)
- ✅ Your application running successfully
- 🧪 Test results (if applicable)
- 💡 Any interesting AI suggestions or corrections

> 📁 Place screenshots in the `docs/screenshots/` folder within each homework directory.

### 🎬 Demo Files

Provide runnable demo scripts where applicable:
- `demo/run.sh` or `demo/run.bat` — Script to start your application
- `demo/test-requests.http` or `demo/requests.sh` — Sample API requests
- `demo/sample-data/` — Any sample data files needed

---

## ▶️ How to Run Applications

Each homework folder should contain clear instructions. 

🔐 Environment Setup should be detailed to run the application.

Add 🧪 Testing guide.

---

## 📊 Grading Criteria

Your submissions will be evaluated on:

| Criteria | Weight | Description |
|----------|--------|-------------|
| ⚙️ **Functionality** | 30% | Does the code work as specified? |
| 📝 **AI Usage Documentation** | 25% | Clear documentation of how AI tools were used |
| 💻 **Code Quality** | 20% | Clean, readable, well-structured code |
| 📚 **Documentation** | 15% | README, comments, and explanations |
| 🎬 **Demo & Screenshots** | 10% | Visual evidence of working solution and AI interaction |

---

### 🗂️ Code Organization

- ✅ Use meaningful file and folder names
- ✅ Separate concerns (routes, services, models)
- ✅ Include `.gitignore` to exclude `node_modules/`, `.env`, etc.

### 📖 Documentation Tips

- Start with a clear problem statement
- Explain your architecture decisions
- Include diagrams where helpful (Mermaid)
- List all dependencies and their purposes

---

**🆘 Getting Help**

Contact the 📚instructor.

Collaborate with 👥 classmates (but submit individual work).

---


<div align="center">

### 🌟 Good luck with your assignments!


</div>
