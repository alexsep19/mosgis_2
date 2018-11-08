define ([], function () {
    
    var grid_name = 'mgmt_contract_object_working_lists_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        var is_editable = data.item._can.edit_work_list

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarInput: false,
                toolbarAdd: is_editable,
            },            

            textSearch: 'contains',

            columns: [                               
                {field: 'id_type', caption: 'Тип', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_contract_object", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/mosgis/_rest/?type=working_lists',
                                    
            onAdd: $_DO.create_mgmt_contract_object_working_lists,
                        
        })

    }
    
})