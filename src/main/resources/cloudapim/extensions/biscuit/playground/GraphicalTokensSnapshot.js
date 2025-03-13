class GraphicalTokensSnapshot extends Component {
  constructor(props) {
    super(props);
    this.state = { snapshot: "" };
  }

  useExample = () => {
    this.setState({
      snapshot:
        "CgkI6AcQZBjAhD0Q72YaZAgEEgVmaWxlMSINEAMaCQoHCAQSAxiACCoQEAMaDAoKCAUSBiCo492qBjIRCg0KAggbEgcIBBIDGIAIEAA6EgoCCgASDAoKCAUSBiCo492qBjoPCgIQABIJCgcIBBIDGIAIQAA=",
    });
  };

  clearSnapshot = () => {
    this.setState({ snapshot: "" });
  };

  render() {
    return React.createElement(
      "div",
      { className: "container py-5" },
      React.createElement("h1", { className: "mb-4 text-white fw-bold text-center" }, "Graphical Snapshot Inspector"),
      React.createElement(
        "div",
        { className: "d-flex flex-wrap justify-content-center gap-3 mb-4" },
        React.createElement(
          "button",
          { className: "btn btn-primary d-flex align-items-center px-4 py-2", onClick: this.useExample },
          React.createElement("i", { className: "fas fa-plus-circle me-2 fs-5" }),
          "Use Snapshot Example"
        ),
        React.createElement(
          "button",
          { className: "btn btn-danger d-flex align-items-center px-4 py-2", onClick: this.clearSnapshot },
          React.createElement("i", { className: "fas fa-trash me-2 fs-5" }),
          "Reset"
        )
      ),
      React.createElement(
        "div",
        { className: "row justify-content-center" },
        React.createElement(
          "div",
          { className: "col-lg-6 col-md-8 col-sm-10 bg-light p-4 rounded shadow" },
          React.createElement("bc-snapshot-printer", {
            snapshot: this.state.snapshot,
            showauthorizer: "true",
            showquery: "true",
          })
        )
      )
    );
  }
}