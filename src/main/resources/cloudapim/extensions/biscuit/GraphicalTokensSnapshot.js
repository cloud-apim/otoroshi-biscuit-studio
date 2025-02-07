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
    this.setState({snapshot: ""})
  }

  render() {
    return React.createElement(
      "div",
      { className: "container text-center mt-4" },
      React.createElement(
        "h1",
        { className: "mb-3 fw-bold" },
        "Graphical Snapshot Inspector"
      ),
      React.createElement(
        "div",
        { className: "d-flex justify-content-center gap-3 mb-4" },
        React.createElement(
          "button",
          {
            className: "btn btn-primary d-flex align-items-center",
            onClick: this.useExample,
          },
          React.createElement("i", { className: "fas fa-plus-circle me-2" }),
          "Use Snapshot example"
        ),
        React.createElement(
          "button",
          {
            className: "btn btn-danger d-flex align-items-center",
            onClick: this.clearSnapshot,
          },
          React.createElement("i", { className: "fas fa-trash me-2" }), 
          "Reset"
        )
      ),
      React.createElement(
        "div",
        { className: "row mb-3 justify-content-center align-items-center" },
        React.createElement(
          "div",
          { className: "col-12 col-sm-6" },
          React.createElement("bc-snapshot-printer", {
            snapshot: this.state.snapshot || "",
            showauthorizer: "true",
            showquery: "true",
          })
        )
      )
    );
  }
}
