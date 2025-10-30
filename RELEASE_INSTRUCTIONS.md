Build & Release — how to get a downloadable app link

1) Create a GitHub repository (if you don't have one)
   - git init
   - git add .
   - git commit -m "Initial commit"
   - git remote add origin https://github.com/<your-username>/<repo>.git
   - git push -u origin main

2) Push a version tag to trigger the workflow
   - Use a semver-style tag (the workflow listens for tags starting with "v"):

     git tag -a v1.0.0 -m "Release v1.0.0"
     git push origin v1.0.0

   - The GitHub Actions workflow at `.github/workflows/build-and-release.yml` will run on macOS and Windows runners. It will:
     - download JavaFX for the runner platform,
     - compile your app,
     - attempt to run `jpackage` to create a native installer (DMG on macOS, MSI on Windows),
     - if jpackage fails, it will create a ZIP containing `Main.jar`.
     - upload the produced files to a GitHub Release attached to the tag.

3) After the workflow completes
   - Open the Releases page for your repo: https://github.com/<your-username>/<repo>/releases
   - Click the newly created release (for example `v1.0.0`) and you'll find the uploaded artifacts (DMG/MSI/ZIP). The public download link will look like:

     https://github.com/<your-username>/<repo>/releases/download/v1.0.0/ExpenseSplitter-1.0.0.dmg

Notes
 - If you want only a ZIP/JAR and not platform installers, the workflow will still upload the JAR/ZIP as a fallback.
 - For macOS notarization and code signing you need an Apple developer account and additional secrets; those are out of scope here.

If you want, I can also:
 - Commit this change and push to a repo you give me, or
 - Walk you through making the repo and pushing the tag step-by-step.
