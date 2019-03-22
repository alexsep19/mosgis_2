define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['general_needs_municipal_resources_grid']

        var t = g.toolbar

        t.disable (b [0])
//        t.disable (b [1])
        
        if (g.getSelection ().length != 1) return

        t.enable (b [g.get (g.getSelection () [0]).is_deleted])

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'general_needs_municipal_resources_grid',

            show: {
                toolbar: true,
                toolbarAdd: !$_USER.role.admin,
                toolbarEdit: !$_USER.role.admin,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: b [0], caption: 'Удалить', onClick: $_DO.delete_general_needs_municipal_resources, disabled: true, off: $_USER.role.admin},
//                    {type: 'button', id: b [1], caption: 'Восстановить', onClick: $_DO.undelete_general_needs_municipal_resources, disabled: true, off: $_USER.role.admin},
                ].filter (not_off),
                
            }, 

            searches: [            
                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'id_ctr_status', caption: 'Статус синхронизации',     type: 'enum', options: {items: data.vc_gis_status.items}},
//                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: !$_USER.role.admin},
            ].filter (not_off),

            columns: [      
            
                {field: 'org.label', caption: 'Организация', size: 100, off: !$_USER.role.admin},

                {field: 'parentcode',  caption: 'Раздел',     size: 10, voc: data.parents},
                {field: 'sortorder', caption: '№п/п', size: 5},
                {field: 'generalmunicipalresourcename', caption: 'Наименование', size: 50},
                {field: 'code_vc_nsi_2',  caption: 'Ресурс',     size: 25, voc: data.vc_nsi_2},
                {field: 'okei',  caption: 'Ед. изм.',     size: 10, voc: data.vc_okei},

                {field: 'elementguid', caption: 'Код в ГИС ЖКХ', size: 10},
                {field: 'id_status',  caption: 'Статус',     size: 50, render: function (r, i, c, v) {
                    var s = data.vc_async_entity_states [v]
                    if (v == 30) {
                        s = '<font color=red>' + s + '</font>: '
                        s += r.out_soap.err_text
                    }
                    return s
                }},

            ].filter (not_off),
            
            url: '/mosgis/_rest/?type=general_needs_municipal_resources',
            
            postData: {data: {uuid_org: $_USER.uuid_org}},
                        
            onAdd:      $_DO.create_general_needs_municipal_resources,            
            onEdit:     $_DO.edit_general_needs_municipal_resources,            
            onDblClick: function (e) {openTab ('/general_needs_municipal_resourcee/' + e.recid)},            
            onRefresh: function (e) {e.done (color_data_mandatory)},            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})