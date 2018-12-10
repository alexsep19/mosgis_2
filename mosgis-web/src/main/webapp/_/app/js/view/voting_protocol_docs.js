define ([], function () {
    
    var grid_name = 'voting_protocol_docs_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
    return function (data, view) {

        function Permissions () {

        if (!data.item.is_deleted && (data.item.id_prtcl_status_gis == 10 || data.item.id_prtcl_status_gis == 11)) {
            if ($_USER.role.admin) return true
                if (data.cach && data.cach.is_own) {
                    if ($_USER.role.nsi_20_1 ||
                        $_USER.role.nsi_20_19 ||
                        $_USER.role.nsi_20_20 ||
                        $_USER.role.nsi_20_21 ||
                        $_USER.role.nsi_20_22)
                        return true
                }
                else if ($_USER.role.nsi_20_8 && $_USER.role['oktmo_' + data.item['fias.oktmo']])
                    return true
            }
            return false
        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: Permissions (),
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: true,
                toolbarDelete: true,
                toolbarEdit: true,
            },            

            textSearch: 'contains',

            columns: [                               
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 15, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_protocol", operator: "is", value: $_REQUEST.id}
            ]},

            url: '/mosgis/_rest/?type=voting_protocol_docs',
            
            onDblClick: $_DO.download_voting_protocol_docs,
            
            onDelete: $_DO.delete_voting_protocol_docs,
            
            onAdd: $_DO.create_voting_protocol_docs,
            
            onEdit: $_DO.edit_voting_protocol_docs,
            
        })

    }
    
})