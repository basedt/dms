# Contributing to Base DMS

Thanks for your interest in Base DMS! We warmly welcome community members to participate in improving the
project, including code contributions ,documentation ,test , issue reporting ,feature suggestions, and more. Our goal is
to develop an AI-powered intelligent data management system.

Please read the following guidelines carefully before contribution. Get started with our [Good first issues](https://github.com/basedt/dms/labels/good%20first%20issue).

## Getting Started

### Fork the repository

Fork the repository on GitHub and clone your fork locally.

```shell
git clone git@github.com:your-username/dms.git
cd dms
```

### Development Setup

Once you have cloned the [GitHub repository](https://github.com/gleiyu/dms), see [how to build](how-to-build.md) for
how to setup your local development environment. Base DMS code is mainly divided into three parts (frontend,backend,docs).

| Module   | Name                                                       | Language |
|----------|------------------------------------------------------------|----------|
| backend  | [dms-api](https://github.com/basedt/dms/tree/main/dms-api) | java     |
| frontend | [dms-ui](https://github.com/basedt/dms/tree/main/dms-ui)   | React    |
| docs     | [dms-web](https://github.com/basedt/dms-web)               | Markdown |

## Contribution guidelines

### Reporting bugs

If a Bug or problem is found in DMS, please open an [issue](https://github.com/basedt/dms/issues) on GitHub. Include as
much detail as possible, such as a clear
description, reproduction steps, and your environment. Please follow the template provided.

### Creating PR (Pull Requests)

1. Create a new branch from main for your changes

```shell
git checkout -b your-branch-name
```

2. Make your changes and commit them

```shell
git commit -m "Your commit message"
```

3. Push your changes to your fork on GitHub

```shell
git push your-branch-name
```

4. After you have pushed your changes, create a pull request (PR) in the DMS repository. 