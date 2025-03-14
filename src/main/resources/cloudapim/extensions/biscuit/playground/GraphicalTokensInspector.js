class GraphicalTokensInspector extends Component {
  constructor(props) {
    super(props);
    this.state = {
      forgeRef: "",
      token: null,
      pubKey: null,
      verifierRef: "",
      errorMessage: null,
      loadedFacts: [],
      showToast: false,
      toastMessage: "",
      toastType: "",
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

  showToast = (message, type) => {
    this.setState({ showToast: true, toastMessage: message, toastType: type });
    setTimeout(() => this.setState({ showToast: false, toastMessage: "", toastType: "" }), 3000);
  };

  resetAll = () => {
    this.setState({ forgeRef: "", verifierRef: "", loadedFacts: [], token: null, pubKey: null });
    this.showToast("Reset successful", "success");
  };

  generateNewToken = () => {
    if (!this.state.forgeRef) {
      this.showToast("Forge is null", "error");
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
          this.showToast(`Error: ${data.error}`, "error");
        } else {
          this.setState({
            pubKey: data.pubKey,
            token: data.token,
          });
          this.showToast("Token generated successfully", "success");
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
          this.showToast(`Error: ${data.error}`, "error");
        } else {
          this.setState({ loadedFacts: data?.datalog || [] });
          this.showToast("Verifier facts loaded", "success");
        }
      });
  };

  render() {
    return React.createElement(
      "div",
      { className: "container py-5 bg-white rounded shadow p-4" },
      React.createElement("h1", { className: "mb-4 fw-bold text-white bg-primary p-3 rounded text-center" }, "Graphical Tokens Inspector"),
      React.createElement(
        "div",
        { className: "row" },
        React.createElement(
          "div",
          { className: "col-md-6" },
          React.createElement("div", { className: "card shadow p-3 bg-white rounded mb-3" },
            React.createElement("div", { className: "card-body" },
              React.createElement(SelectInput, {
                label: "Use a token forge",
                value: this.state.forgeRef,
                onChange: (forgeRef) => this.setState({ forgeRef }),
                valuesFrom: "/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
                transformer: (item) => ({ label: item.name, value: item.id }),
              })
            )
          ),
          React.createElement("button", { className: "btn btn-success w-100 mb-3", onClick: this.generateNewToken },
            React.createElement("i", { className: "fas fa-cogs me-2" }), "Generate Token"
          ),
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
          ),
          React.createElement("button", { className: "btn btn-primary w-100 mb-3", onClick: this.loadVerifierFacts },
            React.createElement("i", { className: "fas fa-cogs me-2" }), "Load Verifier Facts"
          ),
          React.createElement("button", { className: "btn btn-danger w-100", onClick: this.resetAll },
            React.createElement("i", { className: "fas fa-trash-alt me-2" }), "Reset All"
          )
        ),
        React.createElement(
          "div",
          { className: "col-md-6 d-flex align-items-center" },
          React.createElement("div", { className: "card shadow p-4 bg-light w-100" },
            React.createElement("bc-token-printer", {
              showauthorizer: true,
              readonly: true,
              rootPublicKey: this.state.pubKey,
              biscuit: this.state.token,
              authorizer: this.state.loadedFacts.map((line) => line.trim()).join("\n"),
            })
          )
        )
      ),
      this.state.showToast &&
      React.createElement("div", { className: "position-fixed bottom-0 end-0 p-3" },
        React.createElement("div", { className: `toast show text-white ${this.state.toastType === "success" ? "bg-success" : "bg-danger"}`, role: "alert" },
          React.createElement("div", { className: "toast-body" }, this.state.toastMessage)
        )
      )
    );
  }
}