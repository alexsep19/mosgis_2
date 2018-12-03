define ([], function () {

    var b = ['delete', 'undelete']
    
    var is_owner = $_USER.role.nsi_20_1 || $_USER.is_building_society ()
    
    var postData = {}
    if (is_owner) postData.uuid_org = $_USER.uuid_org
    
    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'public_property_contracts_grid',

            show: {
                toolbar: true,
                footer: true,
            },     
            
            toolbar: {
            
                items: !is_owner ? [] : [
                    {type: 'button', id: 'create_org', caption: 'Добавить (юр. лицо)', icon: 'w2ui-icon-plus', onClick: $_DO.create_org_public_property_contracts},
                    {type: 'button', id: 'create_person', caption: 'Добавить (физ. лицо)', icon: 'w2ui-icon-plus', onClick: $_DO.create_person_public_property_contracts},
                ].filter (not_off),
                
            },         
            
/*
            searches: [            
                
                {field: 'docnum', caption: 'Номер',  type: 'text'},
                {field: 'signingdate', caption: 'Дата заключения',  type: 'date'},
                {field: 'id_ctr_status', caption: 'Статус договора', type: 'enum', options: {items: data.vc_gis_status.items.filter (function (i) {
                    switch (i.id) {
                        case 50:
                        case 60:
                        case 80:
                            return false;
                        default:
                            return true;
                    }
                })}}, 
                {field: 'uuid_org', caption: 'Исполнитель', type: 'enum', options: {items: data.vc_orgs.items}, off: $_USER.role.nsi_20_1},
                {field: 'id_customer_type', caption: 'Тип заказчика', type: 'enum', options: {items: data.vc_gis_customer_type.items}, off: !($_USER.role.admin || $_USER.role.nsi_20_1)},
                {field: 'uuid_org_customer', caption: 'Заказчик', type: 'enum', options: {items: data.customers.items}, off: !($_USER.role.admin || $_USER.role.nsi_20_1)},
                {field: 'contractbase', caption: 'Основание заключения', type: 'enum', options: {items: data.vc_nsi_58.items}},
                {field: 'effectivedate', caption: 'Дата вступления в силу',  type: 'date'},
                {field: 'plandatecomptetion', caption: 'Дата окончания',  type: 'date'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),
*/
            columns: [                
            
                {field: 'contractnumber', caption: 'Номер', size: 20},
                {field: 'date_', caption: 'Дата заключения', size: 18, render: _dt},
//                {field: 'id_ctr_status',  caption: 'Статус',     size: 10, voc: data.vc_gis_status},
                {field: 'address', caption: 'Адрес', size: 100},
                {field: 'is_customer_org', caption: 'Тип заказчика', size: 20, voc: {0: 'физ. лицо', 1: 'юр. лицо'}},
                {field: 'customer_label', caption: 'Заказчик', size: 100},
                {field: 'org_label', caption: 'Исполнитель', size: 100, off: is_owner},
//                {field: 'id_customer_type',  caption: 'Вид заказчика',     size: 30, voc: data.vc_gis_customer_type},                
//                {field: 'org_customer.label', caption: 'Заказчик', size: 100},
//                {field: 'contractbase',  caption: 'Основание заключения',     size: 30, voc: data.vc_nsi_58},                
                {field: 'startdate', caption: 'Дата вступления в силу', size: 18, render: _dt},
                {field: 'enddate', caption: 'Дата окончания', size: 18, render: _dt},               
                
            ].filter (not_off),
            
            postData: {data: postData},

            url: '/mosgis/_rest/?type=public_property_contracts',
                        
            onAdd:      $_DO.create_public_property_contracts,            
            
            onDblClick: function (e) {
                openTab ('/public_property_contract/' + e.recid)
            },
            
        }).refresh ();

    }

})