class GraphicalTokensInspector extends Component {
  constructor(props) {
    super(props);
    this.state = {
      forgeRef: "",
      token: null,
      pubKey: null,
      verifierRef: null,
      errorMessage: null,
      loadedFacts: []
    };
  }

  forgesClient = BackOfficeServices.apisClient(
    "biscuit.extensions.cloud-apim.com",
    "v1",
    "biscuit-forges"
  );

  verifiersClient = BackOfficeServices.apisClient(
    "biscuit.extensions.cloud-apim.com",
    "v1",
    "biscuit-verifiers"
  );

  resetAll = () => {
    this.setState({ forgeRef: null, verifierRef: null, loadedFacts: [] })
  }

  generateNewToken = () => {
    if (!this.state.forgeRef) {
      console.log("forge is null");
      return;
    }

    fetch(
      `/extensions/cloud-apim/extensions/biscuit/tokens/forges/${this.state.forgeRef}/_generate`,
      {
        method: "POST",
        credentials: "include",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      }
    )
      .then((d) => d.json())
      .then((data) => {
        if (!data?.done) {
          this.setState({
            isReqLoading: false,
            errorMessage: `Something went wrong : ${data.error}`,
          });
        } else {
          this.setState({
            pubKey: data.pubKey,
            token: data.token,
          });
        }
      });
  };

  loadVerifierFacts = () => {
    console.log("loading authorizer facts ...", this.state.verifierRef);
  }

  render() {
    return React.createElement(
      "div",
      { className: "container py-5 text-center" },
      React.createElement(
        "h1",
        { className: "mb-4 fw-bold text-primary" },
        "Graphical Tokens Inspector"
      ),
      React.createElement(
        "div",
        { className: "mb-3" },
        React.createElement(SelectInput, {
          label: "Use a token forge",
          value: this.state.forgeRef,
          onChange: (forgeRef) => {
            console.log("forgeRef = ", forgeRef);
            this.forgesClient.findById(forgeRef).then((forgeRefById) =>
              console.log("forgeRefById = ", forgeRefById)
            );
            this.setState({ forgeRef });
          },
          valuesFrom:
            "/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
          transformer: (item) => ({ label: item.name, value: item.id }),
        })
      ),
      this.state.forgeRef &&
      React.createElement(
        "button",
        {
          className: "btn btn-success d-flex align-items-center mx-auto px-4 py-2",
          onClick: this.generateNewToken,
        },
        React.createElement("i", { className: "fas fa-cogs me-2 fs-5" }),
        "Generate Token"
      ),
      React.createElement(
        "div",
        { className: "mb-3" },
        React.createElement(SelectInput, {
          label: "Use a token forge",
          value: this.state.verifierRef,
          onChange: (verifierRef) => {
            console.log("forgeRef = ", verifierRef);

            this.setState({ verifierRef });
          },
          valuesFrom:
            "/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers",
          transformer: (item) => ({ label: item.name, value: item.id }),
        })
      ),
      this.state.verifierRef &&
      React.createElement(
        "button",
        {
          className: "btn btn-success d-flex align-items-center mx-auto px-4 py-2",
          onClick: () => {
            this.verifiersClient.findById(this.state.verifierRef).then((verifier) => {
              console.log("verifier info = ", verifier)

              const finalArray = [
                ...(verifier?.config.roles || []),
                ...(verifier?.config.facts || []),
                ...(verifier?.config.acl || []),
              ];

              console.log("final facts array : ", finalArray);

              this.setState({ loadedFacts: finalArray })
            }
            );
          },
        },
        React.createElement("i", { className: "fas fa-cogs me-2 fs-5" }),
        "Load verifier facts"
      ),

      React.createElement(
        "button",
        {
          className: "btn btn-success d-flex align-items-center mx-auto px-4 py-2",
          onClick: this.resetAll,
        },
        React.createElement("i", { className: "fas fa-danger me-2 fs-5" }),
        "Reset all"
      ),

      React.createElement(
        "div",
        { className: "row justify-content-center mt-4" },
        React.createElement(
          "div",
          { className: "col-lg-6 col-md-8 col-sm-10 bg-light p-4 rounded shadow" },
          React.createElement("bc-token-printer", {
            showauthorizer: true,
            readonly: true,
            rootPublicKey: this.state?.pubKey,
            biscuit: this.state?.token,
            authorizer: this.state?.loadedFacts
              .map((line) => line.trim())
              .join("\n"),
          })
        )
      )
    );
  }
}