define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['mgmt_contracts_grid']

        var t = g.toolbar

        t.disable (b [0])
//        t.disable (b [1])
        
        if (g.getSelection ().length != 1) return

        t.enable (b [g.get (g.getSelection () [0]).is_deleted])

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'mgmt_contracts_grid',

            show: {
                toolbar: true,
                toolbarAdd: $_USER.role.nsi_20_1,
//                toolbarEdit: $_USER.role.nsi_20_1,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: b [0], caption: 'Удалить', onClick: $_DO.delete_mgmt_contracts, disabled: true, off: !$_USER.role.nsi_20_1},
//                    {type: 'button', id: b [1], caption: 'Восстановить', onClick: $_DO.undelete_mgmt_contracts, disabled: true, off: $_USER.role.admin},
                ].filter (not_off),
                
            }, 

            searches: [            
//                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
//                {field: 'id_status', caption: 'Статус синхронизации',     type: 'enum', options: {items: data.vc_async_entity_states.items}},
                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: $_USER.role.nsi_20_1},
            ].filter (not_off),

            columns: [                
            
                {field: 'docnum', caption: 'Номер', size: 20},
                {field: 'signingdate', caption: 'Дата заключения', size: 18, render: _dt},
                {field: 'id_ctr_status',  caption: 'Статус',     size: 10, voc: data.vc_gis_status},
                {field: 'org.label', caption: 'Исполнитель', size: 100, off: $_USER.role.nsi_20_1},               
                {field: 'id_customer_type',  caption: 'Вид заказчика',     size: 30, voc: data.vc_gis_customer_type},                
                {field: 'org_customer.label', caption: 'Заказчик', size: 100},
                {field: 'contractbase',  caption: 'Основание заключения',     size: 30, voc: data.vc_nsi_58},                
                {field: 'effectivedate', caption: 'Дата вступления в силу', size: 18, render: _dt},
                {field: 'plandatecomptetion', caption: 'Дата окончания', size: 18, render: _dt},               
                
            ].filter (not_off),
            
            postData: {data: $_USER.role.nsi_20_1 ? {uuid_org: $_USER.uuid_org} : {}},

            url: '/mosgis/_rest/?type=mgmt_contracts',
                        
            onAdd:      $_DO.create_mgmt_contracts,            
//            onEdit:     $_DO.edit_mgmt_contracts,
            
            onDblClick: function (e) {

                openTab ('/mgmt_contract/' + e.recid)

/*                
                if (e.column == 2) {
                    openTab ('/voc_organization_legal/' + this.get (e.recid).uuid_org)
                }
                else {
                    $_DO.edit_mgmt_contracts (e)
                }
*/
            },
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})