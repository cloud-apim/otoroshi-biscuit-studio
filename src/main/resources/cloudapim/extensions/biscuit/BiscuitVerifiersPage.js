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
		'config.facts': {
			type: "array",
			props: { label: "Facts" },
		},
		'config.checks': {
			type: "array",
			props: { label: "Checks" },
		},
		'config.resources': {
			type: "array",
			props: { label: "Resources" },
		},
		'config.rules': {
			type: "array",
			props: { label: "Rules" },
		},
		'config.revocation_ids': {
			type: "array",
			props: { label: "Revocation IDs" },
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
		">>>Revocation IDs",
		"config.revocation_ids",
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
				defaultValue: () => ({
					id: "biscuit-verifier_" + uuid(),
					name: "Biscuit Verifier",
					description: "A simple Biscuit Verifier",
					tags: [],
					metadata: {},
					keypair_ref: "",
          config : {
            checks: [],
            facts: [],
            resources: [],
            rules: [],
            revocation_ids: [],
          }
				}),
				itemName: "Biscuit Verifier",
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
	send = () => {
		fetch("/extensions/cloud-apim/extensions/biscuit/verifiers/_test", {
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
			.then((data) => console.log(data));
	};

	render() {
		return [
			React.createElement(
				"button",
				{
					type: "button",
					className: "btn btn-sm btn-success",
					onClick: this.send,
				},
				React.createElement("i", { className: "fas fa-play" }),
				React.createElement("span", null, " Test Configuration")
			),
		];
	}
}
