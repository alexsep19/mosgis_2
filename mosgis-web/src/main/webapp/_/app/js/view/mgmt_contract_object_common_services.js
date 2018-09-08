define ([], function () {
    
    var grid_name = 'mgmt_contract_object_common_services_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['passport_layout']

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
                toolbarDelete: is_own,
                toolbarEdit: is_own,
            },            
            
            toolbar: {
            
                items: !is_own ? [] : [
                    {type: 'button', id: 'new_house_service', caption: 'Добавить коммунальную услугу', onClick: $_DO.create_mgmt_contract_object_common_services, icon: 'w2ui-icon-plus'},
                    {type: 'button', id: 'new_add_service', caption: 'Добавить дополнительную услугу', onClick: $_DO.create_mgmt_contract_object_common_services, icon: 'w2ui-icon-plus', off: data.tb_add_services.length == 0},
                ].filter (not_off),
                
            }, 
            

            textSearch: 'contains',
            
            columns: [              

                {field: 'is_additional', caption: 'Тип', size: 10, voc: {0: 'Коммунальная', 1: 'Дополнительная'}},
                {field: '_', caption: 'Вид', size: 30, render: function (r) {
                    switch (r.is_additional) {
                        case 0: return data.vc_nsi_3 [r.code_vc_nsi_3]
                        case 1: return data.tb_add_services [r.uuid_add_service]
                    }
                }},
                {field: 'startdate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
                {field: '_', caption: 'Основание', size: 50, render: function (r) {
                    return r.dt ? 'ДС от ' + dt_dmy (r.dt) + ' №' + r.no : 'договор'
                }},
                
            ],
            
            postData: {search: [
                {field: "uuid_contract_object", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/mosgis/_rest/?type=contract_object_services',
                                    
            onDelete: $_DO.delete_mgmt_contract_object_common_services,
            onEdit: $_DO.edit_mgmt_contract_object_common_services,
                        
        })

    }
    
})