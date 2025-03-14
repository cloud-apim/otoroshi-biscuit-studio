class GraphicalDatalogPlayground extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return React.createElement(
      "div",
      { className: "container text-center mt-4" },
      React.createElement(
        "h1",
        { className: "mb-4 fw-bold text-white" },
        React.createElement("i", { className: "fas fa-play-circle me-2" }),
        "Graphical Datalog Playground"
      ),
      React.createElement(
        "div",
        { className: "row mb-3 justify-content-center align-items-center" },
        React.createElement(
          "div",
          { className: "col-12 col-sm-6" },
          React.createElement(
            "div",
            { className: "card shadow-lg border-primary" },
            React.createElement(
              "div",
              { className: "card-header bg-primary text-white" },
              React.createElement("i", { className: "fas fa-cog me-2" }),
              "Settings"
            ),
            React.createElement(
              "div",
              { className: "card-body" },
              React.createElement("bc-datalog-playground", {
                showauthorizer: true,
                showBlocks: true
              })
            )
          )
        )
      )
    );
  }
}