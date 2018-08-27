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
                toolbarAdd: !$_USER.role.admin,
                toolbarEdit: !$_USER.role.admin,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: b [0], caption: 'Удалить', onClick: $_DO.delete_mgmt_contracts, disabled: true, off: $_USER.role.admin},
//                    {type: 'button', id: b [1], caption: 'Восстановить', onClick: $_DO.undelete_mgmt_contracts, disabled: true, off: $_USER.role.admin},
                ].filter (not_off),
                
            }, 

            searches: [            
                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'id_status', caption: 'Статус синхронизации',     type: 'enum', options: {items: data.vc_async_entity_states.items}},
                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: !$_USER.role.admin},
            ].filter (not_off),

            columns: [                
                {field: 'org.label', caption: 'Организация', size: 100, off: !$_USER.role.admin},
                {field: 'label', caption: 'Наименование', size: 50},
                {field: 'okei',  caption: 'Ед. изм.',     size: 10, voc: data.vc_okei},
                {field: 'id_status',  caption: 'Статус',     size: 50, render: function (r, i, c, v) {
                    var s = data.vc_async_entity_states [v]
                    if (v == 30) {
                        s = '<font color=red>' + s + '</font>: '
                        s += r.out_soap.err_text
                    }
                    return s
                }},
            ].filter (not_off),
            
            url: '/mosgis/_rest/?type=mgmt_contracts',
                        
            onAdd:      $_DO.create_mgmt_contracts,            
            onEdit:     $_DO.edit_mgmt_contracts,
            
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