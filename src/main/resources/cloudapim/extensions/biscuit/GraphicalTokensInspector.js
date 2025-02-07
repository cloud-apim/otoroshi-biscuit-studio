class GraphicalTokensInspector extends Component {
  constructor(props) {
		super(props);
		this.state = { forgeRef: "" };
	}

  forgesClient = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-forges"
	);

  render() {
    return React.createElement(
      "div",
      { className: "container text-center mt-4" },
      React.createElement(
        "h1",
        { className: "mb-4 fw-bold" },
        "Graphical Tokens Inspector"
      ),
      React.createElement(
				"div",
				{ className: "form-group" },
				React.createElement(SelectInput, {
					label: "Use a token forge",
					value: this.state.forgeRef,
					onChange: (forgeRef) => {
            console.log("forgeRef = ", forgeRef);

            this.forgesClient.findById(forgeRef)
            .then((forgeRefById) => console.log(forgeRefById))

            this.setState({ forgeRef })
          },
					valuesFrom:
						"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
					transformer: (item) => ({ label: item.name, value: item.id }),
				})
			),
      React.createElement(
        "div",
        { className: "row mb-3 justify-content-center align-items-center" },
        React.createElement(
          "div",
          { className: "col-12 col-sm-6" },
          React.createElement("bc-token-printer", {
            showauthorizer: "true",
          })
        )
      )
    );
  }
}