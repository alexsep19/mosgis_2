define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['municipal_services_grid']

        var t = g.toolbar

        var sel = g.getSelection ()

        if (sel.length != 1 || g.get (sel [0]).is_deleted) {
            t.disable ('edit', 'delete')
        }
        else {
            t.enable ('edit', 'delete')
        }        

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'municipal_services_grid',

            show: {
                toolbar: true,
                toolbarAdd: !$_USER.role.admin,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: 'edit', caption: 'Изменить', onClick: $_DO.edit_municipal_services, disabled: true, off: $_USER.role.admin, icon: 'w2ui-icon-pencil'},
                    {type: 'button', id: 'delete', caption: 'Удалить', onClick: $_DO.delete_municipal_services, disabled: true, off: $_USER.role.admin, icon: 'w2ui-icon-cross'},
                ].filter (not_off),
                
            }, 

            searches: [            
                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},                
                {field: 'code_vc_nsi_3',  caption: 'Вид',     type: 'enum', options: {items: data.vc_nsi_3.items}},
                {field: 'code_vc_nsi_2',  caption: 'Ресурс',  type: 'enum', options: {items: data.vc_nsi_2.items}},                
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},                
                {field: 'id_status', caption: 'Статус синхронизации',     type: 'enum', options: {items: data.vc_async_entity_states.items}},
                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: !$_USER.role.admin},
            ].filter (not_off),

            columns: [                
//                {field: 'uniquenumber', caption: 'Код в ГИС ЖКХ', size: 10},
                {field: 'org.label', caption: 'Организация', size: 100, off: !$_USER.role.admin},
                {field: 'sortorder', caption: '№ п/п', size: 10},
                {field: 'label', caption: 'Наименование', size: 50},
                {field: 'code_vc_nsi_3',  caption: 'Вид',     size: 10, voc: data.vc_nsi_3},
                {field: 'code_vc_nsi_2',  caption: 'Ресурс',  size: 10, voc: data.vc_nsi_2},
//                {field: 'okei',  caption: 'Ед. изм.',     size: 10, voc: data.vc_okei},
                {field: 'id_status',  caption: 'Статус',     size: 50, render: function (r, i, c, v) {
                    var s = data.vc_async_entity_states [v]
                    if (v == 30) {
                        s = '<font color=red>' + s + '</font>: '
                        s += r.out_soap.err_text
                    }
                    return s
                }},
            ].filter (not_off),
            
            url: '/_back/?type=municipal_services',
                        
            onAdd:      $_DO.create_municipal_services,            
            
            onDblClick: function (e) {
                openTab ('/municipal_service/' + e.recid)
            },
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})