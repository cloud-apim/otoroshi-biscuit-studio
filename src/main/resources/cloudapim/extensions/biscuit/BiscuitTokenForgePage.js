class BiscuitTokenForge extends Component {
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
			props: { label: "Name", placeholder: "My Awesome forge" },
		},
		description: {
			type: "string",
			props: {
				label: "Description",
				placeholder: "Description of the forge",
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
    'config.enableTtl': {
      type: 'bool',
      props: { label: 'Enable TTL' },
    },
	  'config.ttl': { type: 'number', props: { label: 'TTL', suffix: 'millis.' } },
    "config.facts": {
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
    "config.resources": {
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
    "config.rules": {
			type: 'array',
      props: {
        component: 
        (props) =>
				React.createElement(
					React.Suspense,
					{ fallback: "Loading..." },
					React.createElement(LazyCodeInput, {
						label: "",
            height: "150px",
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
		remoteFactsLoaderRef: {
			type: "select",
			props: {
				isClearable: true,
				label: "Remote Facts Loader Reference",
				valuesFrom:
					"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-remote-facts",
				transformer: (item) => ({ label: item.name, value: item.id }),
			},
		},
		tokengen: {
			type: TokenGenerator,
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
    "<<<TTL (time to live)",
		"config.enableTtl",
		"config.ttl",
		">>>Facts",
		"config.facts",
		">>>Checks",
		"config.checks",
		">>>Resources",
		"config.resources",
		">>>Rules",
		"config.rules",
		"<<<Remote Facts Loader",
		"remoteFactsLoaderRef",
		">>>Test Token generator",
		"tokengen",
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit Forges`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-forges"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/biscuit-forges",
				defaultTitle: "Biscuit forges",
				defaultValue: () => this.client.template(),
				itemName: "Biscuit Forge",
				formSchema: this.formSchema,
				formFlow: this.formFlow,
				columns: this.columns,
				stayAfterSave: true,
				fetchTemplate: () => this.client.template(),
				fetchItems: (paginationState) => this.client.findAll(),
				updateItem: this.client.update,
				deleteItem: this.client.delete,
				createItem: this.client.create,
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/biscuit-forges/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/biscuit-forges/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitForge",
			},
			null
		);
	}
}

class TokenGenerator extends Component {
	state = {
		token: null,
		errorMessage: null,
		isReqLoading: false,
		pubKey: undefined,
	};

	generateNewToken = () => {
		if (this.props?.rawValue?.keypair_ref && this.props?.rawValue?.config) {
			this.setState({
				errorMessage: null,
				isReqLoading: true,
				pubKey: undefined,
				token: null,
			});
			fetch("/extensions/cloud-apim/extensions/biscuit/tokens/_generate", {
				method: "POST",
				credentials: "include",
				headers: {
					Accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					keypair_ref: this.props.rawValue.keypair_ref,
					remoteFactsLoaderRef: this.props.rawValue.remoteFactsLoaderRef,
					config: this.props.rawValue.config,
				}),
			})
				.then((d) => d.json())
				.then((data) => {
					if (!data?.done) {
						this.setState({
							isReqLoading: false,
							errorMessage: `Something went wrong : ${data.error}`,
						});
					} else {
						this.setState({
							pubKey: data.pubKey,
							token: data.token,
							errorMessage: null,
							isReqLoading: false,
						});
					}
				});
		} else {
			this.setState({
				errorMessage: `no config and no keypair ref provided !`,
			});
		}
	};

	copyToken = () => {
		if (navigator.clipboard && this.state.token) {
			navigator.clipboard.writeText(this.state.token);
		}
	};

	render() {
		const { errorMessage, token, isReqLoading, pubKey } = this.state;

		if (!this.props?.rawValue?.keypair_ref) {
			return [
				React.createElement(
					"div",
					{ className: "row mb-3" },
					React.createElement("div", {
						className: "col-xs-12 col-sm-2 col-form-label",
					}),
					React.createElement(
						"label",
						{ className: "col-sm-10", style: { display: "flex" } },
						"Please select a KeyPair reference before generating a new token"
					)
				),
			];
		}

		return [
			React.createElement(
				"div",
				{ className: "row mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					"Generate biscuit test token"
				),
				React.createElement(
					"div",
					{ className: "col-sm-10", style: { display: "flex" } },
					React.createElement(
						"div",
						{
							style: {
								display: "flex",
								width: "100%",
								flexDirection: "column",
							},
						},
						React.createElement("textarea", {
							ref: (r) => (this.ref = r),
							type: "text",
							rows: 5,
							disabled: true,
							placeholder: "Your new generated token will be displayed here",
							className: "form-control",
							value: token,
						})
					)
				)
			),

			isReqLoading &&
				React.createElement(
					"div",
					{
						className:
							"d-flex flex-column align-items-center justify-content-center text-center",
					},
					React.createElement("div", {
						className: "spinner-border text-white",
						role: "status",
						style: { width: "5rem", height: "5rem" },
					}),
					React.createElement(
						"span",
						{
							className: "mt-3 text-white",
						},
						"The request is being processed"
					)
				),

			errorMessage &&
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
						React.createElement("span", null, ` ${errorMessage}`)
					)
				),
			React.createElement(
				"div",
				{ className: "mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					""
				),
				navigator.clipboard &&
					token &&
					React.createElement(
						"button",
						{
							type: "button",
							className: "btn btn-sm btn-primary",
							onClick: this.copyToken,
						},
						React.createElement("i", { className: "fas fa-clipboard" }),
						React.createElement("span", null, " Copy token to clipboard")
					),
				React.createElement(
					"div",
					{
						className:
							"d-flex flex-column align-items-center justify-content-center text-center",
					},
					React.createElement(
						"button",
						{
							type: "button",
							className: "btn btn-sm btn-success",
							onClick: this.generateNewToken,
							disabled: isReqLoading,
						},
						React.createElement("i", { className: "fas fa-rotate-right" }),
						React.createElement(
							"span",
							{ disabled: this?.props?.rawValue?.keypair_ref },
							"Generate new test token"
						)
					)
				)
			),

			token &&
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
							rootPublicKey: pubKey,
							biscuit: token,
							showauthorizer: "true",
						})
					)
				),
		];
	}
}
