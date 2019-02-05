define ([], function () {
    
    var grid_name = 'mgmt_contract_accounts_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: data.item._can.create_account,
                footer: 1,
                toolbarReload: true,
                toolbarColumns: false,
                toolbarInput: true,
            },            
            
            toolbar: {
            
                items: [
                    {type: 'button', id: 'ind', caption: 'Добавить ЛС физического лица', onClick: $_DO.create_mgmt_contract_accounts, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                    {type: 'button', id: 'org', caption: 'Добавить ЛС юридического лица', onClick: $_DO.create_mgmt_contract_accounts, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                ].filter (not_off),
                
            },
            
            textSearch: 'contains',
            
            columns: [              
                {field: 'accountnumber', caption: 'Номер', size: 100},
            ],
            
            postData: {data: {uuid_contract: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=accounts',
                                    
            onDblClick: function (e) {openTab ('/account/' + e.recid)},
            
            onAdd: $_DO.create_mgmt_contract_accounts,
            
        })

    }
    
})