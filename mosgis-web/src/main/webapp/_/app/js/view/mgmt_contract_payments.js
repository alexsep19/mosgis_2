define ([], function () {
    
    var grid_name = 'mgmt_contract_payments_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_editable = 1 //data.item._can.edit

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_editable,
            },            

            textSearch: 'contains',
            
            columns: [              
                {field: 'begindate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
            ],
            
            postData: {data: {uuid_contract: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=contract_payments',
                                    
            onDblClick: function (e) {openTab ('/mgmt_contract_payment/' + e.recid)},
            
            onAdd: $_DO.create_mgmt_contract_payments,
            
        })

    }
    
})