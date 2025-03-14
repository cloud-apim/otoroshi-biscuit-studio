class GraphicalTokensInspector extends Component {
  constructor(props) {
    super(props);
    this.state = {
      forgeRef: "",
      token: null,
      pubKey: null,
      verifierRef: null,
      errorMessage: null,
      loadedFacts: [],
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
    this.setState({ forgeRef: null, verifierRef: null, loadedFacts: [], token: null, pubKey: null });
  };

  generateNewToken = () => {
    if (!this.state.forgeRef) {
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
      .then((res) => res.json())
      .then((data) => {
        if (!data?.done) {
          this.setState({
            errorMessage: `Something went wrong: ${data.error}`,
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
    fetch(
      `/extensions/cloud-apim/extensions/biscuit/tokens/verifiers/${this.state.verifierRef}/_datalog`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      }
    )
      .then((res) => res.json())
      .then((data) => {
        if (!data?.done) {
          this.setState({
            errorMessage: `Something went wrong: ${data.error}`,
          });
        } else {
          this.setState({ loadedFacts: data?.datalog || [] });
        }
      });
  };

  render() {
    return React.createElement(
      "div",
      { className: "container py-5 text-center" },
      React.createElement("h1", { className: "mb-4 fw-bold text-white bg-primary p-3 rounded" }, "Graphical Tokens Inspector"),
      React.createElement(
        "div",
        { className: "row justify-content-center" },
        React.createElement(
          "div",
          { className: "col-md-6 mb-4" },
          React.createElement("div", { className: "card shadow p-3 bg-white rounded" },
            React.createElement("div", { className: "card-body" },
              React.createElement(SelectInput, {
                label: "Use a token forge",
                value: this.state.forgeRef,
                onChange: (forgeRef) => this.setState({ forgeRef }),
                valuesFrom: "/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
                transformer: (item) => ({ label: item.name, value: item.id }),
              })
            )
          )
        ),
        React.createElement(
          "div",
          { className: "col-md-6 mb-4" },
          React.createElement("div", { className: "card shadow p-3 bg-white rounded" },
            React.createElement("div", { className: "card-body" },
              React.createElement(SelectInput, {
                label: "Use a verifier",
                value: this.state.verifierRef,
                onChange: (verifierRef) => this.setState({ verifierRef }),
                valuesFrom: "/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers",
                transformer: (item) => ({ label: item.name, value: item.id }),
              })
            )
          )
        )
      ),
      this.state.forgeRef &&
      React.createElement("button", { className: "btn btn-primary mb-3 px-4 py-2", onClick: this.generateNewToken },
        React.createElement("i", { className: "fas fa-cogs me-2" }), "Generate Token"
      ),
      this.state.verifierRef &&
      React.createElement("button", { className: "btn btn-info mb-3 px-4 py-2", onClick: this.loadVerifierFacts },
        React.createElement("i", { className: "fas fa-cogs me-2" }), "Load Verifier Facts"
      ),
      React.createElement("button", { className: "btn btn-danger px-4 py-2", onClick: this.resetAll },
        React.createElement("i", { className: "fas fa-trash-alt me-2" }), "Reset All"
      ),
      React.createElement("div", { className: "row justify-content-center mt-4" },
        React.createElement("div", { className: "col-lg-6 col-md-8 col-sm-10 bg-light p-4 rounded shadow" },
          React.createElement("bc-token-printer", {
            showauthorizer: true,
            readonly: true,
            rootPublicKey: this.state.pubKey,
            biscuit: this.state.token,
            authorizer: this.state.loadedFacts.map((line) => line.trim()).join("\n"),
          })
        )
      )
    );
  }
}