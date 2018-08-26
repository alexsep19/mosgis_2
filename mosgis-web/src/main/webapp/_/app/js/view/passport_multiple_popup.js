define ([], function () {
    
    var records
    
    function get_id () {
        return $('body').data ('data').id
    }

    function grid_from () {
        return w2ui ['voc_rd_nsi_popup_grid_available']
    }

    function grid_to () {
        return w2ui ['voc_rd_nsi_popup_grid_set']
    }

    $_F5 = function () {
                
        var ids = $_SESSION.get ('ids')

        grid_from ().records = records.filter (function (r) {
            return !ids [r.recid]
        })
        
        grid_to   ().records = records.filter (function (r) {
            return  ids [r.recid] == 1
        })

        grid_from ().refresh ()
        grid_to   ().refresh ()

    }

    return function (data, view) {
        
        records = data.voc.items

        $(view).w2uppop ({title: data.def.label}, function () {
            
            $('#w2ui-popup .w2ui-form').w2reform ({
            
                name: 'passport_multiple_popup_form',
                
                record: data.record,
    
                fields : [                
                ],
                
//                focus: 1,
                                
            });
    
            clickOn ($('#w2ui-popup button[name=update]'), $_DO.update_passport_multiple_popup)
            
            clickOn ($('#w2ui-popup button[name=close]'), $_DO.close_passport_multiple_popup)
            
            function _add () {
                var ids = $_SESSION.get ('ids')
                ids [grid_from ().getSelection () [0]] = 1
                $_SESSION.set ('ids', ids)
                sessionStorage.setItem ('is_dirty', 1)
                $_F5 ()                
            }

            function _del () {
                var ids = $_SESSION.get ('ids')
                delete ids [grid_to ().getSelection () [0]]
                $_SESSION.set ('ids', ids)
                sessionStorage.setItem ('is_dirty', 1)
                $_F5 ()                
            }
            
            $('button[name=add]').click (_add)
            $('button[name=del]').click (_del)
            
            $('button[name=add_all]').click (function () {
                var ids = $_SESSION.get ('ids')
                $.each (grid_from ().records, function () {ids [this.recid] = 1})
                $_SESSION.set ('ids', ids)
                sessionStorage.setItem ('is_dirty', 1)
                $_F5 ()
            })            
            
            $('button[name=del_all]').click (function () {
                $_SESSION.set ('ids', {})
                sessionStorage.setItem ('is_dirty', 1)
                $_F5 ()
            })            
            
            $('#grid_set').w2regrid ({ 

                name: 'voc_rd_nsi_popup_grid_set',             

                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: false,
                },            

                columns: [                
                   {field: 'label', caption: 'Наименование', size: 100},
                ],

                records: [],
                
                onSelect: function (e) {
                    $('button[name=del]').prop ('disabled', false)
                },
                
                onUnselect: function (e) {
                    $('button[name=del]').prop ('disabled', true)
                },
                
                onDblClick: _del,

            }).refresh ();
                        
            $('#grid_available').w2regrid ({ 

                name: 'voc_rd_nsi_popup_grid_available',             

                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: false,
                },            

                columns: [                
                   {field: 'label', caption: 'Наименование', size: 100},
                ],

                records: [],
                
                onSelect: function (e) {
                    $('button[name=add]').prop ('disabled', false)
                },
                
                onUnselect: function (e) {
                    $('button[name=add]').prop ('disabled', true)
                },
                
                onDblClick: _add,

            }).refresh ()
                        
            $_F5 ()
    
        })

    }    
    
})