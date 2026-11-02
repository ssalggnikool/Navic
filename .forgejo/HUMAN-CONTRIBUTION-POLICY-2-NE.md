# SciActive Human Contribution Policy

Version 2-NE (No Exceptions), 2 September 2026
<https://sciactive.com/human-contribution-policy/>

## Preamble

The SciActive Human Contribution Policy is a free, general contribution policy
meant to safeguard projects from the inherent legal and security risks of AI
generated contributions. It is also meant to ensure that the trust projects'
users place in the projects and their maintainers is not inadvertently violated
by the inclusion of untrusted material. The inclusion of AI generated material
in projects presents significant known and unknown risks to the longevity and
security of the projects. This policy is meant to declare that the human
authorship of any material introduced in covered projects is both desired and
ensured.

The purpose of this document is to define a clear boundary between human
authorship and machine generation, and clearly declare that a project will only
accept contributions that are human authored. If a project chooses to adopt and
adhere to this policy, it is a clear indication that the project will not accept
AI generated contributions and that its users can be assured that, once adopted,
the contributions to the project are authored by humans, and humans alone,
except when this policy allows specific exceptions.

## Definitions

"This policy" refers to version 2-NE of the SciActive Human Contribution Policy.

"The project" means the collective body of work covered by this policy,
including but not limited to, the source code, object code, documentation,
scripts, configuration files, graphical assets, supporting material, policy
documents, translations, and processes. This does not include source codes and
object codes of dependencies that are merely linked to, not stored, within the
project, and any portions of object code resulting from the compilation of the
imported source codes of linked dependencies when compiled during the normal
build process of the project.

"AI generated" means that the subject material is in whole, or in meaningful
part, the output of a generative AI model or models, such as a Large Language
Model. This does not include code that is the result of non-generative tools,
such as standard compilers, linters, or basic IDE auto-completions. This does,
however, include code that is the result of code block generators and automatic
refactoring tools that make use of generative AI models.

"Contribution" means any material submitted to the project with the intention of
being included in, or distributed with, the project. A contribution may be
material that is presented to a review process, such as a pull request, or
material that is submitted into any of the project's material tracking or
distribution systems, such as a source control commit or submission to a content
management system, even by a project team member or maintainer.

"Contributor" means the natural person or legal entity ultimately responsible
for submitting the contribution. The contributor, in this case, cannot be an
automated agent. In the case that an automated agent submits or attempts to
submit a contribution to the project, the natural person or legal entity
responsible for initiating the agent is the contributor.

"User" means the person or entity who ultimately uses or is intended to
ultimately use the project or its product.

## Policy

1. Contributions must be human authored. AI generated contributions are not
   permitted and will not be accepted.
   a. There are no exceptions to this requirement.
2. If a contributor attempts to submit AI generated material without disclosing
   that the material is AI generated, the contribution will be considered a
   deceptive contribution, and action may be taken against the contributor.

## Reasoning

### Legal Implications of AI Generated Contributions

#### License Laundering

Generative AI models are trained on publicly available code that may be subject
to restrictive licensing conditions. These models may produce verbatim, near
verbatim, or derivative snippets of this code, which would therefore be subject
to the license terms under which that code was released. If these snippets are
introduced into a code base with an incompatible license, that code base's
licensing could become legally unenforceable, or the maintainers of the code
base could even be subject to lawsuits.

This policy helps protect the project from having incompatibly licensed code
introduced into the project's code base.

#### Copyright Ineligibility

As of March 2026, the US Copyright Office and several international bodies
maintain that AI generated material, without significant human "creative
control", cannot be copyrighted. This means that AI generated material often
resides in the public domain, making it impossible to enforce any license terms
on the material.

This policy helps protect the project from having material from the public
domain introduced into the project.

#### Compliance with New Laws

New regulations may impose strict control or new liability for AI generated
material. This may include, and may not be limited to, legal liabilities
concerning security vulnerabilities or "hallucinated" sections of AI generated
code.

This policy helps protect the project from becoming in violation of any new
laws, or liable in new ways, with regard to any new legislation that may govern
the use or distribution of AI generated material.

### Technical and Security Implications of AI Generated Contributions

#### Prevalence of Security Vulnerabilities

AI generated code may be more likely than human authored code to introduce
security vulnerabilities. These vulnerabilities can be very difficult to
recognize during the code review process.

This policy helps protect the project from the introduction of new security
vulnerabilities.

#### Dependency Hallucinations and Slopsquatting

AI generated code often includes references to non-existent dependencies. These
references are commonly called "hallucinations". A new type of attack has arisen
that involves an attacker registering a package whose name is frequently
hallucinated. When AI generated code containing this hallucination is accepted,
and this dependency is installed, the attacker can ship malicious code into the
project's build, introducing a major security vulnerability. This type of attack
has become known as "slopsquatting".

This policy helps protect the project from both hallucinated dependencies and
slopsquatting attacks.

#### Architectural Drift

AI generated material frequently mirrors industry-standard practices and lacks
the context of the project's overall architecture and practices, resulting in
code that can duplicate efforts, neglect the project's own practices, and
deviate from the project's overall planned architecture, and documentation that
is inconsistent with the project's existing standards and practices. As more of
a project becomes AI generated material, the original architecture can become
more obscured, resulting in a code base that is often no longer coherent and
difficult to return back to the original "vision".

This policy helps protect the project from incurring this kind of technical
debt.

### Ethical and Community Implications of AI Generated Contributions

#### Erosion of Trust

The users of the project have an implicit trust that the maintainers of the
project understand the code contained within the project's code base. AI
generated code, which may not even be fully understood by the contributor,
breaks this implicit trust. AI generated code is the result of an automated
process, not intentional authorship. Allowing this code into the project's code
base would mean the maintainers of the project may not fully understand the code
base.

This policy helps protect the project from eroding the trust the users have
placed into the project and its maintainers.

#### Maintenance Exhaustion

AI generated material can be created much faster than it can be reviewed. This
imbalance can lead to a heavy burden on maintainers of reviewing an abundance of
AI generated material that often has the appearance of high quality material,
but contains many flaws. The difficulty of properly reviewing these
contributions, along with the tremendous volume of these contributions, can lead
maintainers to feel "burnt out" and leave the project.

This policy helps protect the project from incurring this heavy maintenance
load.

## Use of This Policy

This policy is distributed under the Creative Commons Attribution-ShareAlike 4.0
International (CC BY-SA 4.0) license, available at
https://creativecommons.org/licenses/by-sa/4.0/. Anyone can share and reuse this
policy, in its original form or modified, under the conditions of the
CC BY-SA 4.0 license. The full legal text of the license is available at
https://creativecommons.org/licenses/by-sa/4.0/legalcode.en. If you modify this
policy, rename the policy in order to avoid the implication that the modified
policy is endorsed by SciActive Inc.

SciActive Human Contribution Policy © 2026 by SciActive Inc is licensed
under CC BY-SA 4.0
