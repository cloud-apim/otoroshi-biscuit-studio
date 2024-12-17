class BiscuitRbacPoliciesPage extends Component {
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
		roles: {
			type: "object",
			props: { label: "List of Roles" },
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
		"<<<Roles",
		"roles",
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit RBAC Policies`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-rbac"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/rbac",
				defaultTitle: "All Biscuit RBAC Policies",
				defaultValue: () => ({
					id: "biscuit_rbac_policy_" + uuid(),
					name: "Biscuit RBAC Policy" + uuid(),
					description: "A simple Biscuit RBAC Policy",
					tags: [],
					metadata: {},
					roles: {
						admin: [
							"billing:read",
							"billing:write",
							"address:read",
							"address:write",
						],
						accounting: ["billing:read", "billing:write", "address:read"],
						support: ["address:read", "address:write"],
						pilot: ["spaceship:drive", "address:read"],
						delivery: [
							"address:read",
							"package:load",
							"package:unload",
							"package:deliver",
						],
					},
				}),
				itemName: "Biscuit RBAC Policy",
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
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/rbac/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/rbac/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitRBAC",
			},
			null
		);
	}
}
