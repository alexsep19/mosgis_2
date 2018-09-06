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
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_own,
            },            

            textSearch: 'contains',
            
            columns: [              
                {field: 'fias.label', caption: 'Адрес', size: 100},
                {field: 'startdate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
                {field: '_', caption: 'Основание', size: 50, render: function (r) {
                    return r.dt ? 'ДС от ' + dt_dmy (r.dt) + ' №' + r.no : 'договор'
                }},
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