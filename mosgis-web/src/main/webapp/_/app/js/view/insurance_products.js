define ([], function () {

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['insurance_products_grid']

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

            name: 'insurance_products_grid',

            show: {
                toolbar: true,
                toolbarAdd: !$_USER.role.admin,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: 'edit', caption: 'Изменить', onClick: $_DO.edit_insurance_products, disabled: true, off: $_USER.role.admin, icon: 'w2ui-icon-pencil'},
                    {type: 'button', id: 'delete', caption: 'Удалить', onClick: $_DO.delete_insurance_products, disabled: true, off: $_USER.role.admin, icon: 'w2ui-icon-cross'},
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
                {field: 'label', caption: 'Описание', size: 50},
                {field: 'org_ins.label', caption: 'Страховщик', size: 100},
                {field: 'name', caption: 'Имя файла', size: 50, attr: 'data-ref=1'},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'id_status',  caption: 'Статус',     size: 50, render: function (r, i, c, v) {
                    var s = data.vc_async_entity_states [v]
                    if (v == 30) {
                        s = '<font color=red>' + s + '</font>: '
                        s += r.out_soap.err_text
                    }
                    return s
                }},
                {field: 'uniquenumber', caption: 'Код в ГИС ЖКХ', size: 50},
                
            ].filter (not_off),
            
            url: '/_back/?type=insurance_products',
                        
            onAdd:      $_DO.create_insurance_products,            
            onEdit:     $_DO.edit_insurance_products,

            onClick: function (e) {
            
                var grid = this
                var col = grid.columns [e.column]
                
                if (col.field == 'name') $_DO.download_insurance_products (e)
            
            },            
            
            onDblClick: function (e) {

                openTab ('/insurance_product/' + e.recid)

/*                
                if (e.column == 2) {
                    openTab ('/voc_organization_legal/' + this.get (e.recid).uuid_org)
                }
                else {
                    $_DO.edit_insurance_products (e)
                }
*/
            },
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})