class GraphicalDatalogPlayground extends Component {
  render() {
    return React.createElement(
      "div",
      { className: "container text-center mt-4" },
      React.createElement(
        "h1",
        { className: "mb-4 fw-bold" },
        "Graphical Datalog Playground"
      ),
      React.createElement(
        "div",
        { className: "row mb-3 justify-content-center align-items-center" },
        React.createElement(
          "div",
          { className: "col-12 col-sm-6" },
          React.createElement("bc-datalog-playground", {
            showauthorizer: "true",
            showBlocks: "true"
          })
        )
      )
    );
  }
}