class BiscuitAttenuatorPage extends Component {
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
			props: { label: "Name", placeholder: "My Awesome Biscuit Attenuator" },
		},
		description: {
			type: "string",
			props: {
				label: "Description",
				placeholder: "Description of the Biscuit Attenuator",
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
		"config.checks": {
			type: 'array',
      props: {
        component: 
        (props) =>
				React.createElement(
					React.Suspense,
					{ fallback: "Loading..." },
					React.createElement(LazyCodeInput, {
						label: "",
            height: "50px",
						mode: "prolog",
						value: props.itemValue,
						onChange: (e) => {
							const arr = props.value;
							arr[props.idx] = e;
							props.onChange(arr);
						},
					})
				)
      }
		},
		tester: {
			type: BiscuitAttenuatorTester,
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
		"metadata",
		"tags",
		"<<<KeyPair",
		"keypair_ref",
		"<<<Checks",
		"config.checks",
		">>>Tester",
		"tester",
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit Attenuators`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-attenuators"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/attenuators",
				defaultTitle: "All Biscuit Attenuators",
				defaultValue: () => this.client.template(),
				itemName: "Biscuit Attenuator",
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
					} else if (e?.checks?.length === 0) {
						alert("Your checks array is empty, please add check rules");
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
					} else if (e?.checks?.length === 0) {
						alert("Your checks array is empty, please add check rules");
					} else {
						return this.client.create(e);
					}
				},
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/attenuators/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/attenuators/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitAttenuator",
			},
			null
		);
	}
}

class BiscuitAttenuatorTester extends Component {
	constructor(props) {
		super(props);
		this.state = {
			attenuatedToken: undefined,
			tokenInput: undefined,
			errorMesage: "",
			pubKey: undefined,
			forgeRef: undefined,
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
		this.setState({
			error: "",
			attenuatedToken: "",
			errorMesage: "",
		});

		fetch(
			"/extensions/cloud-apim/extensions/biscuit/tokens/attenuators/_test",
			{
				method: "POST",
				credentials: "include",
				headers: {
					Accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					checks: [...this.props.rawValue?.config?.checks],
					keypair_ref: this.props.rawValue?.keypair_ref,
					forge_ref: forgeRef,
					token: tokenInput,
				}),
			}
		)
			.then((r) => r.json())
			.then((data) => {
				if (!data?.token) {
					this.setState({
						errorMesage: `Something went wrong during attenuation ${data?.error}`,
						attenuatedToken: null,
						pubKey: null,
					});
				} else {
					this.setState({
						errorMesage: null,
						pubKey: data.pubKey,
						attenuatedToken: data.token,
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
		const { attenuatedToken, tokenInput, errorMesage, pubKey, forgeRef } =
			this.state;

		return React.createElement("div", { className: "row mb-3" }, [
			React.createElement(
				"div",
				{ className: "form-group" },
				React.createElement(SelectInput, {
					label: "Use a token forge",
					isClearable: true,
					value: forgeRef,
					onChange: (forgeRef) => this.setState({ forgeRef }),
					valuesFrom:
						"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
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
			attenuatedToken &&
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
						React.createElement("bc-token-printer", {
							readonly: true,
							biscuit: attenuatedToken,
							rootPublicKey: pubKey,
							showauthorizer: "true",
						})
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
