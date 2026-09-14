# Intended Use, Integration, and Security Considerations

> [!IMPORTANT]
> EMF JSON-Jackson is a **library** that provides a JSON binding for EMF. It is **embedded into an application** that controls its own input and supplies its own EMF configuration. It is **not** a service, and it establishes **no** trust boundary of its own.
>
> If your application processes models that originate from untrusted sources, **you are responsible** for adding the controls described in [Integrating responsibly](#integrating-responsibly).

## What EMF JSON-Jackson is

EMF JSON-Jackson is a **building block** for working with EMF models in JSON. It implements EMF's `Resource` so that models can be read from and written to JSON, using [Jackson](https://github.com/FasterXML/jackson) for the underlying data binding.

It is intended to be:

- **embedded in an application** that owns its own input and decides which payloads are acceptable;
- **driven by the `ResourceSet` and EMF configuration the application supplies**;
- a **mapping between JSON and EMF** that follows EMF semantics, so that models behave the same way they do with other EMF resource implementations.

## What EMF JSON-Jackson is not

It is **not**:

- a service, an endpoint, or anything that receives input on its own;
- an authentication or authorization layer;
- a validation layer for payloads or models;
- a sandbox that defends against malicious payloads.

## Security model: the embedding application owns the trust boundary

EMF JSON-Jackson operates entirely on the `ResourceSet` it is given. It has no notion of users, sessions, or trust boundaries, and it applies no policy of its own to the models it processes. Every decision about what may be loaded, from where, and on whose behalf is made by the embedding application through the EMF configuration it supplies.

Within that trust boundary the library **performs no**:

- authentication or authorization of the party that supplied a payload;
- validation of otherwise well-formed input against malicious intent;
- confinement of model resolution beyond what the supplied `ResourceSet` permits.

As a consequence, and **by design**, reading a payload resolves the URIs it contains through the application's `ResourceSet`. These are **intended capabilities** of an EMF resource implementation, not security weaknesses, and they rely on the application keeping untrusted payloads *outside* its trust boundary.

## Standard EMF resolution semantics

In EMF, model elements are identified by URI and resolved through a `ResourceSet`. This applies to metamodel elements such as classifiers as well as to ordinary model elements. EMF JSON-Jackson builds on this mechanism instead of defining one of its own.

Consequently, every URI in a payload is resolved the same way, whether it identifies the type of an object or references another model element. Resolution goes through `ResourceSet.getEObject`, which falls back to EMF's demand loading for URIs the `ResourceSet` does not already know, and from there to the `URIConverter`. This is what enables cross-document references and dynamic metamodels.

Which URIs can be resolved is therefore determined by the `ResourceSet` the application supplies, specifically by its **package registry** and its **`URIConverter`**. EMF JSON-Jackson adds no resolution policy of its own. Such a policy would duplicate the mechanisms EMF already provides for this purpose and would break the use cases named above.

## Integrating responsibly

If your application loads models from an untrusted or partially trusted source, treat the payload as **input that has not been vetted by the library**, and supply the controls it does not provide itself. Recommended layers (defense in depth):

- **Establish trust before loading**: authenticate and authorize the source of a payload, and reject payloads from any other origin.
- **Register the metamodels you expect**: populate the `ResourceSet`'s package registry with the `EPackage`s your application supports, so that expected models resolve without demand loading.
- **Constrain resolution**: configure the `ResourceSet`'s `URIConverter` to resolve only those resources your application considers legitimate, and handle the resulting resolution failures, which surface as load errors or unresolved proxies.
- **Validate against your expectations**: where the payload structure is known in advance, validate incoming data against it before loading.
- **Least privilege**: run model loading with the minimum network and file system access your use case requires.

## A note on these assumptions

This security model reflects EMF JSON-Jackson's intended use as an embedded serialization library. Adopters whose integration differs should **re-evaluate** these assumptions, and it is the adopter's responsibility to add the appropriate controls before processing input from outside their trust boundary.

## Reporting a vulnerability

Please report suspected vulnerabilities through the process described in [`SECURITY.md`](../SECURITY.md). Do not open public issues, pull requests, or discussions for them.

Reports describing behavior that is only reachable when the library is used contrary to the guidance above (for example, loading unvetted payloads into an unrestricted `ResourceSet`) are treated as **integration-hardening** matters rather than defects in the component. Issues that are exploitable **within** the intended usage are handled as described in `SECURITY.md`.
