# Contributing to Eclipse EMF.cloud: EMF JSON-Jackson

Thank you for your interest in the EMF JSON-Jackson project!
The following is a set of guidelines for contributing to EMF JSON-Jackson.

## Code of Conduct

This project is governed by the [Eclipse Community Code of Conduct](CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code.

## Communication

The following communication channels are available:

* [GitHub issues](https://github.com/eclipse-emfcloud/emfjson-jackson/issues) - for bug reports, feature requests, etc.
* [GitHub Discussions](https://github.com/eclipse-emfcloud/emfcloud/discussions) - for questions
* [Developer mailing list](https://accounts.eclipse.org/mailing-list/emfcloud-dev) - for organizational issues (e.g. elections of new committers)

In case you have a question, please look into the [GitHub Discussions](https://github.com/eclipse-emfcloud/emfcloud/discussions) first.
If you don't find any answer there, feel free to start a new discussion or create a new [issue](https://github.com/eclipse-emfcloud/emfjson-jackson/issues) to get help.

## How to Contribute

In order to contribute, please first open an issue in this project.
This issue should describe the bug you intend to fix or the feature you would like to add.
Once you have your code ready for review, please open a pull request in the respective repository.
A [committer of the EMF.cloud project](https://projects.eclipse.org/projects/ecd.emfcloud/who) will then review your contribution and help to get it merged.

Please note that before your pull request can be accepted, you must electronically sign the [Eclipse Contributor Agreement](https://www.eclipse.org/legal/ECA.php).
For more information, see the [Eclipse Foundation Project Handbook](https://www.eclipse.org/projects/handbook/#resources-commit).

### Branch names and commit messages

If you are an [elected committer of the EMF.cloud project](https://projects.eclipse.org/projects/ecd.emfcloud/who) please create a branch in the repository directly.
Otherwise please fork and create a branch in your fork for the pull request.

The branch name should be in the form `issues/{issue_number}`, e.g. `issues/16`. So please create an issue before creating a pull request.
All branches with this naming schema will be deleted after they are merged.

In the commit message you should also reference the corresponding issue, e.g. using `closes https://github.com/eclipse-emfcloud/emfjson-jackson/issues/16`, thus allowing [auto close of issues](https://help.github.com/en/github/managing-your-work-on-github/closing-issues-using-keywords).
Please use the absolute URL of the issue instead of just `#16`, as using the absolute URL will allow to correctly reference issues irrespectively from where you open the pull request.

Please make sure you read the [guide for a good commit message](https://chris.beams.io/posts/git-commit/).

## Guide for Commiters

>As a committer, can I directly merge the PR that seems relevant to me, or should I make sure with the rest of the team that it can be inserted in the sprint's scope ?
 
For your own PRs a positive peer review of another committer is required before merging. If you are reviewing external PRs it depends on the size of the change. 
For simple bug fixes, documentation updates etc. just go ahead and merge them after you have given your approval. For larger or conflicting changes (or if you are unsure) 
please tag one of the project leads (@eneufeld, @planger, @koegel ).

>How can I check that the team is not in the middle of a release process nor in a code freeze ?
 
In case of a pending release or other code freeze there should be a general announcement on the default communication channels (discussions + dev mailing list).

>Is there any manual action I should take regarding https://github.com/orgs/eclipse-emfcloud/projects/2 ? Or is only the project leader in charge of it ?
 
Managing the project board is mostly the responsibility of the project leads. However, there are a couple of actions you can take to ease the process for them:
If possible, always create an accompanying issue for your PR. Issues are easier to track and manage in the project board
When you create new issues please directly add them to the `EMFCloud` project. Please self-assign if you plan to work on the issue.
If your PR resolves the corresponding issue please add a `Fixes` notice to the commit message/PR description. (e.g. `Fixes #123` or `Closes #123)
