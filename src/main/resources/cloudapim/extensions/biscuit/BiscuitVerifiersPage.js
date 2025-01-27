class BiscuitVerifiersPage extends Component {
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
		keypair_ref: {
			type: "select",
			props: {
				label: "Key Pair Reference",
				valuesFrom:
					"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs",
				transformer: (item) => ({ label: item.name, value: item.id }),
			},
		},
		"config.facts": {
			type: "array",
			props: { label: "Facts" },
		},
		"config.checks": {
			type: "array",
			props: { label: "Checks" },
		},
		"config.resources": {
			type: "array",
			props: { label: "Resources" },
		},
		"config.rules": {
			type: "array",
			props: { label: "Rules" },
		},
		"config.policies": {
			type: "array",
			props: { label: "Policies" },
		},
		"config.revokedIds": {
			type: "array",
			props: { label: "Revoked IDs" },
		},
		tester: {
			type: BiscuitVerifierTester,
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
		"<<<KeyPair",
		"keypair_ref",
		">>>Facts",
		"config.facts",
		">>>Checks",
		"config.checks",
		">>>Resources",
		"config.resources",
		">>>Rules",
		"config.rules",
		">>>Policies",
		"config.policies",
		">>>Revoked IDs",
		"config.revokedIds",
		">>>Tester",
		"tester",
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit Verifiers`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-verifiers"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/verifiers",
				defaultTitle: "All Biscuit Verifiers",
				defaultValue: () => this.client.template(),
				itemName: "Biscuit Verifier",
				formSchema: this.formSchema,
				formFlow: this.formFlow,
				columns: this.columns,
				stayAfterSave: true,
				fetchTemplate: () => this.client.template(),
				fetchItems: (paginationState) => this.client.findAll(),
				updateItem: (e) => {
					if (!e.keypair_ref) {
						alert(
							"Could not update entity if the keypair reference is not provided"
						);
					} else {
						return this.client.update(e);
					}
				},
				deleteItem: this.client.delete,
				createItem: (e) => {
					if (!e.keypair_ref) {
						alert(
							"Could not create entity if the keypair reference is not provided"
						);
					} else {
						return this.client.create(e);
					}
				},
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/verifiers/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/verifiers/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitVerifier",
			},
			null
		);
	}
}
class BiscuitVerifierTester extends Component {
	constructor(props) {
		super(props);
		this.state = {
			forgeRef: undefined,
			tokenInput: undefined,
			errorMesage: "",
			successMessage: "",
		};
	}

	handleInputChange = (event) => {
		this.setState({ biscuitToken: event.target.value });
	};

	send = () => {
		const { forgeRef, tokenInput } = this.state;

		// Validate that either a Biscuit token or a provider is provided
		if (!forgeRef && !tokenInput && forgeRef !== "" && tokenInput !== "") {
			this.setState({
				error: "Please provide either a Biscuit token or select a provider.",
			});
			return;
		}

		// Clear previous errors and warnings
		this.setState({ error: "", warning: "", successMessage: "" });

		fetch("/extensions/cloud-apim/extensions/biscuit/tokens/verifier/_test", {
			method: "POST",
			credentials: "include",
			headers: {
				Accept: "application/json",
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				config: { ...this.props.rawValue?.config },
				keypairRef: this.props.rawValue?.keypair_ref,
				biscuitForgeRef: forgeRef,
				biscuitToken: tokenInput,
			}),
		})
			.then((r) => r.json())
			.then((data) => {
				if (!data?.done) {
					this.setState({
						successMessage: null,
						errorMesage: `Bad verification : ${data.error}`,
					});
				} else {
					this.setState({
						errorMesage: null,
						successMessage: data.message,
					});
				}
			})
			.catch((error) => {
				this.setState({
					errorMesage: "An error occurred while processing your request.",
				});
			});
	};

	render() {
		const { errorMesage, successMessage, forgeRef, tokenInput } = this.state;

		return React.createElement("div", { className: "row mb-3" }, [
			React.createElement(
				"div",
				{ className: "form-group" },
				React.createElement(SelectInput, {
					label: "Use a token forge",
					value: forgeRef,
					onChange: (forgeRef) => this.setState({ forgeRef }),
					valuesFrom:
						"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/tokens-forge",
					transformer: (item) => ({ label: item.name, value: item.id }),
				})
			),
			React.createElement(
				"div",
				{
					style: { maxWidth: "80%", marginLeft: "15%", textAlign: "center" },
				},
				React.createElement(
					"div",
					{ className: "form-group" },
					React.createElement("label", null, "Biscuit token bas64 encoded"),
					React.createElement("textarea", {
						type: "text",
						rows: 5,
						placeholder: "Your biscuit base64 encoded token",
						className: "form-control",
						value: tokenInput,
						onChange: (e) => this.setState({ tokenInput: e.target.value }),
					})
				)
			),
			errorMesage &&
				React.createElement(
					"div",
					{
						style: { maxWidth: "80%", marginLeft: "15%", textAlign: "center" },
					},
					React.createElement(
						"div",
						{
							className: "alert alert-danger rounded mx-auto",
							style: { width: "100%", textAlign: "center" },
						},
						React.createElement("i", {
							className: "fas fa-exclamation-circle",
						}),
						React.createElement("span", null, ` ${errorMesage}`)
					)
				),
			successMessage &&
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
						React.createElement("span", null, ` ${successMessage}`)
					)
				),
			React.createElement(
				"div",
				{ className: "text-center" },
				React.createElement(
					"button",
					{
						type: "button",
						className: "btn btn-sm btn-success",
						onClick: this.send,
					},
					React.createElement("i", { className: "fas fa-play" }),
					React.createElement("span", null, " Test Configuration")
				)
			),
		]);
	}
}
