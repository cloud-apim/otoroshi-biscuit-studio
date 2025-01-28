class BiscuitRemoteFactsLoaderPage extends Component {
	formSchema = {
		_loc: {
			type: "location",
			props: {},
		},
		id: {
			type: "string",
			disabled: true,
			props: { label: "Id", placeholder: "---" },
		},
		name: {
			type: "string",
			props: { label: "Name", placeholder: "My Awesome Context" },
		},
		description: {
			type: "string",
			props: {
				label: "Description",
				placeholder: "Description of the Context",
			},
		},
		metadata: {
			type: "object",
			props: { label: "Metadata" },
		},
		tags: {
			type: "array",
			props: { label: "Tags" },
		},
		"config.api_url": {
			type: "string",
			props: {
				label: "API URL",
				placeholder: "Description of the Context",
			},
		},
		"config.method": {
			type: "select",
			props: {
				label: "API Method",
				possibleValues: [
					{ label: "POST", value: "POST" },
					{ label: "PUT", value: "PUT" },
					{ label: "PATCH", value: "PATCH" }
				],
			},
		},
		"config.timeout": {
			type: "number",
			props: {
				label: "API timeout",
				suffix: "millis",
			},
		},
		"config.headers": {
			type: "object",
			props: { label: "Headers" },
		},
		tester: {
			type: RemoteFactsTester,
		},
	};

	columns = [
		{
			title: "Name",
			filterId: "name",
			content: (item) => item.name,
		},
		{
			title: "Description",
			filterId: "description",
			content: (item) => item.description,
		},
		{
			title: "Created At",
			filterId: "metadata.created_at",
			content: (item) => item?.metadata?.created_at,
		},
	];

	formFlow = [
		"_loc",
		"id",
		"name",
		"description",
		">>>Metadata and tags",
		"tags",
		"metadata",
		"<<<Configuration",
		"config.api_url",
		"config.method",
		"config.headers",
		"config.timeout",
		"<<<Tester",
		"tester",
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit Remote Facts Loader`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-remote-facts"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/remote-facts",
				defaultTitle: "All Biscuit Remote Facts",
				defaultValue: () => this.client.template(),
				itemName: "Biscuit Remote Facts Loader",
				formSchema: this.formSchema,
				formFlow: this.formFlow,
				columns: this.columns,
				stayAfterSave: true,
				fetchTemplate: () => this.client.template(),
				fetchItems: (paginationState) => this.client.findAll(),
				updateItem: (e) => {
					if (
						!e.config.api_url ||
						e.config.api_url === "https://my-api.domain.com/v1/roles"
					) {
						alert("Please verify your API URL connection");
					} else {
						return this.client.update(e);
					}
				},
				deleteItem: this.client.delete,
				createItem: (e) => {
					if (
						!e.config.api_url ||
						e.config.api_url === "https://my-api.domain.com/v1/roles"
					) {
						alert("Please verify your API URL connection");
					} else {
						return this.client.create(e);
					}
				},
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitRemoteFactsLoader",
			},
			null
		);
	}
}

class RemoteFactsTester extends Component {
	state = {
		isTesting: false,
		isLoadedSuccess: false,
		loadedFacts: [],
		errorMessage: "",
	};

	reset = () => {
		this.setState({
			isTesting: true,
			isLoadedSuccess: false,
			errorMessage: "",
			loadedFacts: [],
		});
	};

	handleTest = () => {
		this.reset();

		fetch("/extensions/cloud-apim/extensions/biscuit/remote-facts/_test", {
			method: "POST",
			credentials: "include",
			headers: {
				Accept: "application/json",
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				...this.props.rawValue?.config,
			}),
		})
			.then((r) => r.json())
			.then((data) => {
				if (data.error) {
					this.setState({
						isTesting: false,
						isLoadedSuccess: false,
						error: true,
						errorMessage: data.error,
					});
				} else {
					const finalArray = [
						...(data?.loadedFacts.roles || []),
						...(data?.loadedFacts.facts || []),
						...(data?.loadedFacts.acl || []),
					];

					this.setState({
						isTesting: false,
						isLoadedSuccess: true,
						loadedFacts: finalArray,
					});
				}
			})
			.catch((err) => {
				this.setState({
					isTesting: false,
					isLoadedSuccess: false,
					errorMessage: err,
				});
			});
	};

	handleTokenGenerated = (event) => {
		const token = event.detail.token; // Assuming the token is passed in the detail object
		console.log("Generated Token:", token);
		// Perform additional actions with the generated token
	};

	render() {
		return [
			React.createElement(
				"div",
				{ className: "row mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					""
				),
				React.createElement(
					"div",
					{ className: "col-sm-10", style: { display: "flex" } },
					React.createElement(
						"div",
						{ style: { width: "100%" }, className: "input-group" },
						React.createElement(
							"button",
							{
								type: "button",
								className: "btn btn-sm btn-success",
								onClick: this.handleTest,
								disabled: this.state.isTesting,
							},
							React.createElement("i", { className: "fas fa-play" }),
							React.createElement("span", null, " Test")
						)
					)
				)
			),
			this.state.errorMessage &&
				React.createElement(
					"div",
					{
						style: { maxWidth: "80%", marginLeft: "15%", textAlign: "center" },
					},
					React.createElement(
						"div",
						{
							className: "alert alert-danger rounded mx-auto",
							style: { maxWidth: "75%", textAlign: "center" },
						},
						React.createElement("i", {
							className: "fas fa-exclamation-triangle",
						}),
						React.createElement("span", null, ` ${this.state.errorMessage}`)
					)
				),

			this.state.isLoadedSuccess &&
				React.createElement(
					"div",
					{
						style: { maxWidth: "80%", marginLeft: "15%", textAlign: "center" },
					},
					React.createElement(
						"div",
						{
							className: "alert alert-success rounded mx-auto",
							style: { maxWidth: "75%", textAlign: "center" },
						},
						React.createElement("i", {
							className: "fas fa-check",
						}),
						React.createElement("span", null, `Facts loaded successfully !`)
					)
				),

			this.state.isLoadedSuccess &&
				this.state?.loadedFacts &&
				React.createElement(
					"div",
					{
						style: { maxWidth: "80%", marginLeft: "15%", textAlign: "center" },
					},
					React.createElement(
						"div",
						{ className: "row mb-3" },
						React.createElement(
							"label",
							{ className: "col-xs-12 col-sm-2 col-form-label" },
							"Biscuit Playground test"
						),
						React.createElement(
							"bc-token-printer",
							{
								showauthorizer: "true",
								authorizer: this.state?.loadedFacts
									.map((line) => line.trim())
									.join("\n"),
							},
							""
						)
					)
				),
		];
	}
}
