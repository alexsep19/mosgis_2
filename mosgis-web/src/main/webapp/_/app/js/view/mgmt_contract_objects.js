define ([], function () {
    
    var grid_name = 'mgmt_contract_objects_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_own = (data.item.uuid_org = $_USER.uuid_org)

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_own,
            },            

            textSearch: 'contains',
            
            columns: [              
//                {field: 'objectnumber', caption: 'Номер', size: 20},
                {field: 'startdate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
            ],
            
            postData: {search: [
                {field: "uuid_contract", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/mosgis/_rest/?type=contract_objects',
                        
//            onDelete: $_DO.delete_mgmt_contract_objects,
            
            onAdd: $_DO.create_mgmt_contract_objects,
                        
        })

    }
    
})