#!/bin/bash

# Exit on error
set -e

# Configuration
VERSION_FILE="app/build.gradle.kts"
MAIN_BRANCH="main"

echo "🚀 Starting release tagging process..."

# 1. Check current branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "$MAIN_BRANCH" ]; then
    echo "❌ Error: You must be on the '$MAIN_BRANCH' branch to release (currently on '$CURRENT_BRANCH')."
    exit 1
fi

# 2. Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
    echo "❌ Error: You have uncommitted changes. Please commit or stash them before releasing."
    exit 1
fi

# 3. Pull latest changes
echo "📥 Pulling latest changes from $MAIN_BRANCH..."
git pull origin "$MAIN_BRANCH"

# 4. Extract version from build.gradle.kts
# Look for versionName = "X.Y"
VERSION_NAME=$(grep "versionName =" "$VERSION_FILE" | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "$VERSION_NAME" ]; then
    echo "❌ Error: Could not find versionName in $VERSION_FILE"
    exit 1
fi

TAG_NAME="v$VERSION_NAME"

# 5. Check if tag already exists
if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
    echo "⚠️  Warning: Tag $TAG_NAME already exists."
    read -p "Do you want to overwrite it? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 1
    fi
    git tag -d "$TAG_NAME"
    git push origin --delete "$TAG_NAME" || true
fi

# 6. Create and push tag
echo "🏷️  Creating tag $TAG_NAME..."
git tag -a "$TAG_NAME" -m "Release $TAG_NAME"
echo "📤 Pushing tag $TAG_NAME to origin..."
git push origin "$TAG_NAME"

echo "✅ Release $TAG_NAME tagged and pushed successfully!"
