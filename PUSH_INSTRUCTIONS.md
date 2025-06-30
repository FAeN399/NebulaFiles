# How to Push to GitHub from Termux

## Option 1: GitHub Personal Access Token (Recommended)

1. Go to https://github.com/settings/tokens
2. Click "Generate new token" → "Generate new token (classic)"
3. Give it a name like "Termux"
4. Select scopes: `repo` (full control)
5. Generate and copy the token

Then push using:
```bash
git push https://YOUR_GITHUB_USERNAME:YOUR_TOKEN@github.com/FAeN399/NebulaFiles.git main
```

Or set it permanently:
```bash
git remote set-url origin https://YOUR_GITHUB_USERNAME:YOUR_TOKEN@github.com/FAeN399/NebulaFiles.git
git push -u origin main
```

## Option 2: GitHub CLI (gh)

Install GitHub CLI:
```bash
pkg install gh
```

Authenticate:
```bash
gh auth login
```

Then push:
```bash
git push -u origin main
```

## Option 3: SSH Key

Generate SSH key:
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
```

Add to GitHub:
1. Copy the public key: `cat ~/.ssh/id_ed25519.pub`
2. Go to https://github.com/settings/keys
3. Add new SSH key

Change remote to SSH:
```bash
git remote set-url origin git@github.com:FAeN399/NebulaFiles.git
git push -u origin main
```

## Quick Manual Upload

If you prefer, you can also:
1. Create a zip: `zip -r NebulaFiles.zip . -x ".git/*"`
2. Go to https://github.com/FAeN399/NebulaFiles
3. Click "uploading an existing file"
4. Drag and drop the zip file